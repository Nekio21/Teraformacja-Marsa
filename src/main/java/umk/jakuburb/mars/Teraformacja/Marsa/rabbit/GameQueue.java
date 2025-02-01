package umk.jakuburb.mars.Teraformacja.Marsa.rabbit;

import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import umk.jakuburb.mars.Teraformacja.Marsa.database.entity.Card;
import umk.jakuburb.mars.Teraformacja.Marsa.database.repository.CardRepository;
import umk.jakuburb.mars.Teraformacja.Marsa.message.*;
import umk.jakuburb.mars.Teraformacja.Marsa.message.Error;
import umk.jakuburb.mars.Teraformacja.Marsa.message.gameData.GameData;
import umk.jakuburb.mars.Teraformacja.Marsa.utils.Timerable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static umk.jakuburb.mars.Teraformacja.Marsa.message.Area.NOTHING;
import static umk.jakuburb.mars.Teraformacja.Marsa.message.Area.NO_OCEAN;
import static umk.jakuburb.mars.Teraformacja.Marsa.message.MessageType.*;

public class GameQueue extends GameCoreQueue implements Timerable {

    private CardRepository cardRepository;
    private HashMap<MessageType, Function<MyMessage, Error>> checkAndSave;
    private GameData gameDate;

    private HashMap<String, Boolean> activePlayers;
    private HashMap<String, Long> mainCardToChose;
    private HashMap<String, List<Long>> cardsToChose;
    private HashMap<String, UserState> usersState;

    private List<Long> drawCards;

    public static final String NAME = "GameQueue";
    private String playersExchange;
    private final int RoundTimeSetting = 60;
    private int price = 3;
    private int roundTime = 60;
    private int whoPlay = 0;
    private GameState gameState = GameState.WAITING;


    //TODO: o bakup nie sie ma co sie martwic bo przeciez dziala fifo
    //wiec jak beda dwa taski: backup, nowa wiadomosc
    //to jak najpierw bedzie backup to zrobi backup i potem doda nowa wiadomosc,
    //jesli na odwrot to najpierw da nowa wiadomosc, a potem backup, który bedzie juz z ta nowa wiadomoscia

    public GameQueue(String uniqName,String playersExchange, RabbitAdmin rabbitAdmin, RabbitTemplate rabbitTemplate) {
        super(uniqName, rabbitAdmin, rabbitTemplate);

        gameDate = new GameData();
        this.playersExchange = playersExchange;
        drawCards = new ArrayList<>();
        checkAndSave = new HashMap<>();
        activePlayers = new HashMap<>();
        mainCardToChose = new HashMap<>();
        cardsToChose = new HashMap<>();
        usersState = new HashMap<>();

        addReceiveFunction(MessageType.RECOVER, this::recover);


        addReceiveFunction(MessageType.MESSAGE_SEND, this::monitorUserMove);
        checkAndSave.put(MessageType.MESSAGE_SEND, this::messageSendCAS);

        addReceiveFunction(MessageType.PLAYER_IN_GAME, this::playerInGame);
        //checkAndSave.put(MessageType.PLAYER_IN_GAME, this::playerInGameCAS);

        addReceiveFunction(MessageType.MAIN_CARDS, this::mainCard);
        addReceiveFunction(CARDS10, this::card10Receive);
    }

    public void initGameData(List<String> players){
        gameDate.setPlayers(players);

        for(String player: gameDate.getPlayers()){
            gameDate.getResources().put(player, new Resources());
            gameDate.getCards().put(player, new ArrayList<>());
            gameDate.getMainCards().put(player, -99L);
            gameDate.getUsedCardBlue().put(player, new ArrayList<>());
            gameDate.getUsedCardRed().put(player, new ArrayList<>());
            gameDate.getUsedCardGreen().put(player, new ArrayList<>());
            gameDate.getLevel().put(player, 0L);

            usersState.put(player, UserState.WAITING);
        }

        gameDate.setPlanet(List.of(
                NO_OCEAN, NOTHING, NOTHING,NO_OCEAN,NO_OCEAN,
                NOTHING, NOTHING, NOTHING,NOTHING,NOTHING, NO_OCEAN,
                NOTHING, NOTHING, NOTHING,NOTHING,NOTHING, NOTHING, NOTHING,
                NOTHING, NOTHING, NOTHING,NOTHING,NOTHING, NOTHING, NOTHING,NO_OCEAN,
                NOTHING, NOTHING, NOTHING,NO_OCEAN,NO_OCEAN, NO_OCEAN, NOTHING,NOTHING,NOTHING,
                NOTHING, NOTHING, NOTHING,NOTHING,NOTHING, NO_OCEAN, NO_OCEAN,NO_OCEAN,
                NOTHING, NOTHING, NOTHING,NOTHING,NOTHING, NOTHING, NOTHING,
                NOTHING, NOTHING, NOTHING,NOTHING,NOTHING, NOTHING,
                NOTHING, NOTHING, NOTHING,NO_OCEAN,NOTHING
        ));
    }

    private void card10Receive(MyMessage msg){
        //List<CardToSend> carts = msg.getCards();

        //List<Long> ids = carts.stream().map(CardToSend::getIndex).toList();

        if(usersState.get(msg.getFrom()) != UserState.CHOSE_CARD) return;

        List<Long> ids = msg.getMsg().stream().map(Long::valueOf).toList();

        int gold = gameDate.getResources().get(msg.getFrom()).getGold();
        String from = msg.getFrom();

        if(ids.size()*price > gold){
            sendErrorMessage(from, Error.MONEY);
        }

        //if(Arrays.deepEquals(ids.toArray(), cardsToChose.get(from).toArray())){
        if(cardsToChose.get(from).containsAll(ids)){
            gameDate.getCards().put(from, ids);
//            msg.setMessageType(CARDS10_SAVE);
//            msg.setAbout(msg.getFrom());
//            sendToUser(msg);
            sendCards(from, ids);
            sendResources(from);
            usersState.put(from, UserState.WAITING);

            if(usersState.values().stream().allMatch(e->e==UserState.WAITING)){
                sendGameState(from, GameState.ROUND);
            }else{
                sendState(from, UserState.WAITING);
            }
        }else{
            sendErrorMessage(from, Error.DEFAULT);
        }
    }

    public void startGame(){

    }

    public void mainCard(MyMessage message){
        if(Long.valueOf(message.getMsg().get(0)).equals(mainCardToChose.get(message.getFrom()))){
            String from = message.getFrom();

            gameDate.getMainCards().put(from, Long.valueOf(message.getMsg().get(0)));
            //gameDate
            //TODO: niech karta glowna zmieni resources

            message.setMsg(List.of(from));
            message.setMessageType(MessageType.MAIN_CARDS_SAVE);
            message.setAbout(from);

            sendToUsers(message);
            sendResources(from);
            sendCards10(message);
        } else{
            sendErrorMessage(message.getFrom(), Error.DEFAULT);
        }
    }

    private void sendState(String user, UserState userState){
        MyMessage myMessage = new MyMessage();
        myMessage.setFrom(NAME);
        myMessage.setAbout(user);
        myMessage.setMessageType(USER_STATE);
        myMessage.setMsg(List.of(userState.toString()));

        sendToUsers(myMessage);
    }

    private void sendGameState(String user, GameState gameState){
        MyMessage myMessage = new MyMessage();
        myMessage.setFrom(NAME);
        myMessage.setAbout(user);
        myMessage.setMessageType(GAME_STATE);
        myMessage.setMsg(List.of(gameState.toString()));

        sendToUsers(myMessage);
    }

    private void sendResources(String user){
        MyMessage myMessage = new MyMessage();
        myMessage.setFrom(NAME);
        myMessage.setAbout(user);
        myMessage.setMessageType(MessageType.RESOURCES);
        myMessage.setResources(gameDate.getPlayers().stream().map(e->gameDate.getResources().get(e)).toList());
        myMessage.setOwners(gameDate.getPlayers());

        sendToUsers(myMessage);
    }

    private void sendCards(String user, List<Long> cardToSends){
        List<Card> cards = cardRepository.getCards(cardToSends);

        MyMessage myMessage = new MyMessage();
        myMessage.setFrom(NAME);
        myMessage.setAbout(user);
        myMessage.setMessageType(MessageType.CARDS);
        myMessage.setCards(cards.stream().map(e->new CardToSend(e.getId(), e.getTypeCard(), e.getImage())).toList());
        myMessage.setOwners(gameDate.getPlayers());

        sendToUsers(myMessage);
    }

    private void sendCards10(MyMessage message){
        usersState.put(message.getAbout(), UserState.CHOSE_CARD);

        int amount = 3;
        List<Card> cardsList = cardRepository.getRandomCards(drawCards, amount);

        if(cardsList.size() != amount){
            sendErrorMessage(message.getAbout(), Error.NO_MORE_CARD);
            return;
        }

        drawCards.addAll(cardsList.stream().map(Card::getId).toList());
        cardsToChose.put(message.getAbout(),cardsList.stream().map(Card::getId).toList());

        List<CardToSend> cardToSends = cardsList.stream().map(c->new CardToSend(c.getId(), c.getTypeCard(), c.getImage())).toList();

        MyMessage myMessage = new MyMessage();
        myMessage.setCards(cardToSends);
        myMessage.setMessageType(MessageType.CARDS10);
        myMessage.setFrom(message.getFrom());
        myMessage.setAbout(message.getAbout());

        sendToUser(myMessage);
    }

    public void recover(MyMessage message){
        String from = message.getMsg().get(0);
        String addressQueue = CoreQueue.PRESUB_BEFORE_NAME + from;

        MyMessage msg = new MyMessage();
        msg.setFrom(NAME);
        msg.setMessageType(MessageType.RECOVER);
        msg.setGameData(gameDate);

        rabbitTemplate.convertAndSend(addressQueue, msg);
        System.out.println("helped :)");
    }

    public Error messageSendCAS(MyMessage message){
        ChatRecord chatRecord = new ChatRecord(message.getFrom(), message.getMsg().get(0));
        gameDate.getChat().add(chatRecord);

        return Error.NO_ERROR;
    }

    private void playerInGame(MyMessage msg){
        activePlayers.put(msg.getFrom(), true);

        if(gameState != GameState.WAITING){
            return;
        }

        boolean all = gameDate.getPlayers().stream().allMatch(e->activePlayers.getOrDefault(e, false));

        gameState = all ? GameState.CHOSE_CARDS : GameState.WAITING;

        MyMessage msg2 = new MyMessage();
        msg2.setFrom(NAME);
        msg2.setMessageType(GAME_STATE);
        msg2.setMsg(List.of(gameState.toString()));
        rabbitTemplate.convertAndSend(playersExchange, "", msg2);

        if(gameState == GameState.CHOSE_CARDS) {
            giveMainCards();
        }

    }

    private void giveMainCards(){
        List<Card> mainCards =  cardRepository.getRandomMainCard();
        String addressQueue;

        for(int i=0;i<gameDate.getPlayers().size();i++){
            usersState.put(gameDate.getPlayers().get(i), UserState.CHOSE_MAIN_CARD);
            addressQueue = CoreQueue.PRESUB_BEFORE_NAME + gameDate.getPlayers().get(i);

            CardToSend card = new CardToSend();
            card.setTypeCard(mainCards.get(i).getTypeCard());
            card.setImage(mainCards.get(i).getImage());
            card.setIndex(mainCards.get(i).getId());

            mainCardToChose.put(gameDate.getPlayers().get(i), mainCards.get(i).getId());

            sendGameState(gameDate.getPlayers().get(i), GameState.CHOSE_CARDS);

            MyMessage msg2 = new MyMessage();
            msg2.setFrom(NAME);
            msg2.setMessageType(MessageType.MAIN_CARDS);
            msg2.setCards(List.of(card));
            rabbitTemplate.convertAndSend(addressQueue, msg2);
        }
    }

    @Override
    protected MyMessage notFoundTypeReceive(MyMessage m) {

        return null;
    }

    @Override
    public void doThing() {
        MyMessage myMessage = new MyMessage();
        myMessage.setFrom(NAME);
        myMessage.setMessageType(MessageType.CLOCK);
        myMessage.setMsg(List.of(gameDate.getPlayers().get(whoPlay), String.valueOf(roundTime)));

        rabbitTemplate.convertAndSend(playersExchange, "", myMessage);

        roundTime--;

        if(roundTime < 0){
            roundTime = RoundTimeSetting;
            whoPlay = (whoPlay+1)%gameDate.getPlayers().size();
        }
    }

    public GameData getGameDate() {
        return gameDate;
    }

    public void setGameDate(GameData gameDate) {
        this.gameDate = gameDate;
    }

    public Error checkAndSaveDefault(MyMessage message){
        return Error.DEFAULT;
    }

    public boolean checkAndSaveDefaultTrue(MyMessage message){
        return true;
    }

    public void monitorUserMove(MyMessage message){
        //wysyła recover
        for(String uniqPlayersName: gameDate.getPlayers()){
            recover(new MyMessage(List.of(uniqPlayersName)));
        }

        //sprawdza czy moze
        //zapisuje rzeczy ...
        Error err = checkAndSave.getOrDefault(message.getMessageType(), this::checkAndSaveDefault).apply(message);

        //przekazuje informacje
        if(err == Error.NO_ERROR){
            rabbitTemplate.convertAndSend(playersExchange, "", message);
        }else{
            sendErrorMessage(message.getFrom(), err);
        }
    }

    private void sendErrorMessage(String from, Error error){
        MyMessage myMessage = new MyMessage();
        myMessage.setMessageType(MessageType.ERROR);
        myMessage.setFrom(from);
        myMessage.setAbout(from);
        myMessage.setError(error);

        sendToUser(myMessage);
    }

    private void sendToUser(MyMessage message){
        String from = message.getAbout();
        String addressQueue = CoreQueue.PRESUB_BEFORE_NAME + from;

        message.setFrom(NAME);

        rabbitTemplate.convertAndSend(addressQueue, message);
    }

    private void sendToUsers(MyMessage message){
        message.setFrom(NAME);
        rabbitTemplate.convertAndSend(playersExchange, "", message);
    }

    public void setCardRepository(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }
}
