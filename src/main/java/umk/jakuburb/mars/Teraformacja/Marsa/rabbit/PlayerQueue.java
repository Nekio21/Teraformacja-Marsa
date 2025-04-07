package umk.jakuburb.mars.Teraformacja.Marsa.rabbit;

import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import umk.jakuburb.mars.Teraformacja.Marsa.database.entity.Card;
import umk.jakuburb.mars.Teraformacja.Marsa.database.repository.CardRepository;
import umk.jakuburb.mars.Teraformacja.Marsa.message.*;
import umk.jakuburb.mars.Teraformacja.Marsa.message.gameData.GameData;
import umk.jakuburb.mars.Teraformacja.Marsa.message.gameData.GameDataCheck;
import umk.jakuburb.mars.Teraformacja.Marsa.message.gameData.GameDataProces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static umk.jakuburb.mars.Teraformacja.Marsa.message.CardToSend.makeListCardToSend;

public class PlayerQueue extends CoreQueue{

    private CardRepository cardRepository;

    public PlayerQueue(String uniqName,CardRepository cardRepository, RabbitAdmin rabbitAdmin, RabbitTemplate rabbitTemplate){
        super(uniqName, rabbitAdmin, rabbitTemplate);

        this.cardRepository = cardRepository;

        addSendFunction(MessageType.USER_IN, this::sendIAmIn);
        addSendFunction(MessageType.USER_OUT, this::sendIAmOut);

        addReceiveFunction(MessageType.USER_IN, this::dontTouch);
        addReceiveFunction(MessageType.USER_OUT, this::dontTouch);

        addReceiveFunction(MessageType.LOBBY_QUIT, this::dontTouchNotMe);

        addSendFunction(MessageType.USER_ACTIVE, this::userActiveSend);
        addSendFunction(MessageType.USER_INACTIVE, this::userInactiveSend);

        addReceiveFunction(MessageType.USER_ACTIVE, this::dontTouch);
        addReceiveFunction(MessageType.USER_INACTIVE, this::dontTouch);

        addReceiveFunction(MessageType.GAME_CREATED, this::dontTouch);

        addSendFunction(MessageType.MESSAGE_SEND, this::messageSend);
        addReceiveFunction(MessageType.MESSAGE_SEND, this::messageReceive);

        addReceiveFunction(MessageType.CLOCK, this::clock);
        addReceiveFunction(MessageType.RECOVER, this::recover);

        addSendFunction(MessageType.MAIN_CARDS, this::sendToGame);
        addReceiveFunction(MessageType.MAIN_CARDS_SAVE, this::save);
        //addSendFunction(MessageType.RECOVER, this::recoverSend);
        addSendFunction(MessageType.CARDS10, this::sendToGame);

        addReceiveFunction(MessageType.RESOURCES, this::save);
        addReceiveFunction(MessageType.CARDS, this::save);

        addSendFunction(MessageType.USE_CARD, this::sendToGame);
        addSendFunction(MessageType.NEXT_ROUND, this::sendToGame);

        addReceiveFunction(MessageType.USE_CARD, this::save);
        addReceiveFunction(MessageType.OTHERS, this::save);

        addSendFunction(MessageType.TEMP_UP, this::sendToGame);
        addSendFunction(MessageType.BOARD_TREE, this::sendToGame);
        addSendFunction(MessageType.BOARD_OCEAN, this::sendToGame);
        addSendFunction(MessageType.BOARD_CITY, this::sendToGame);
        addSendFunction(MessageType.PUT_TREE, this::sendToGame);
        addSendFunction(MessageType.PUT_CITY, this::sendToGame);
        addSendFunction(MessageType.PUT_OCEAN, this::sendToGame);

        addSendFunction(MessageType.TITLE, this::sendToGame);
        addReceiveFunction(MessageType.TITLE, this::save);

        addSendFunction(MessageType.PRIZE, this::sendToGame);
        addReceiveFunction(MessageType.PRIZE, this::save);

        addSendFunction(MessageType.PS, this::sendToGame);

        gameData.getCards().put(uniqName, new ArrayList<>());
    }

    private void messageSend(MyMessage send){
        MyMessage message = new MyMessage();

        message.setFrom(uniqName);
        message.setMessageType(MessageType.MESSAGE_SEND);
        message.setMsg(List.of(send.getMsg().get(0), uniqName));

        rabbitTemplate.convertAndSend(addressMap.get(Adress.GAMECORE),"help", message);
    }

    private MyMessage messageReceive(MyMessage send){
        this.gameData.getChat().add(new ChatRecord(send.getFrom(), send.getMsg().get(0)));

        if(send.getFrom().equals(uniqName)){
                MyMessage msg = new MyMessage();
                msg.setFrom(uniqName);
                msg.setMessageType(MessageType.MESSAGE_SEND);
                msg.setMsg(List.of(send.getMsg().get(0)));

                return msg;
        }else{
            return send;
        }
    }

    public void gameIn(){
        gameData = new GameData();
        userInGame();
        recoverSend();
    }
    private void recoverSend(){
        MyMessage msg = new MyMessage();
        msg.setFrom(uniqName);
        msg.setMessageType(MessageType.RECOVER);
        msg.setMsg(List.of(uniqName));

        rabbitTemplate.convertAndSend(addressMap.get(Adress.GAMECORE),"help", msg);
    }

    private void userInGame(){
        MyMessage msg = new MyMessage();
        msg.setFrom(uniqName);
        msg.setMessageType(MessageType.PLAYER_IN_GAME);
        msg.setMsg(List.of(uniqName));

        rabbitTemplate.convertAndSend(addressMap.get(Adress.GAMECORE),"help", msg);
    }



    private MyMessage recover(MyMessage msg){
        GameData gameData = msg.getGameData();

        List<GameDataCheck> check = GameDataProces.check(gameData, this.gameData, uniqName);

        for(GameDataCheck gdc: check) {
            MyMessage msgToSend = new MyMessage();

            msgToSend.setFrom(uniqName);
            msgToSend.setMessageType(MessageType.RECOVER);
            msgToSend.setRecoverType(gdc);

            switch (gdc){
                case PLAYERS -> {
                    msgToSend.setMsg(gameData.getPlayers());
                    this.gameData.setPlayers(gameData.getPlayers());
                }
                case CHAT -> {
                    msgToSend.setChat(gameData.getChat());
                    this.gameData.setChat(gameData.getChat());
                }
                case MAIN_CARD -> {
                    List<Long> ids = gameData.getPlayers().stream().map(e->gameData.getMainCards().get(e)).toList();

                    msgToSend.setOwners(gameData.getPlayers());
                    msgToSend.setDataLong(ids);
                    msgToSend.setCards(CardToSend.makeCardToSend(cardRepository.getCards(ids)));
                    this.gameData.setMainCards(gameData.getMainCards());
                }
                case USED_CARDS_BLUE -> {
                    List<List<Long>> ids = gameData.getPlayers().stream().map(e->gameData.getUsedCardBlue().get(e)).toList();
                    List<List<Card>> cards = gameData.getPlayers().stream().map(e->cardRepository.getCards(gameData.getUsedCardBlue().get(e))).toList();

                    msgToSend.setOwners(gameData.getPlayers());
                    msgToSend.setDataListLong(ids);
                    msgToSend.setListCards(makeListCardToSend(cards));
                    this.gameData.setUsedCardBlue(gameData.getUsedCardBlue());
                }
                case USED_CARDS_RED ->{
                    List<List<Long>> ids = gameData.getPlayers().stream().map(e->gameData.getUsedCardRed().get(e)).toList();
                    List<List<Card>> cards = gameData.getPlayers().stream().map(e->cardRepository.getCards(gameData.getUsedCardRed().get(e))).toList();

                    msgToSend.setOwners(gameData.getPlayers());
                    msgToSend.setDataListLong(ids);
                    msgToSend.setListCards(makeListCardToSend(cards));
                    this.gameData.setUsedCardRed(gameData.getUsedCardRed());
                }
                case USED_CARDS_GREEN ->{
                    List<List<Long>> ids = gameData.getPlayers().stream().map(e->gameData.getUsedCardGreen().get(e)).toList();
                    List<List<Card>> cards = gameData.getPlayers().stream().map(e->cardRepository.getCards(gameData.getUsedCardGreen().get(e))).toList();

                    msgToSend.setOwners(gameData.getPlayers());
                    msgToSend.setDataListLong(ids);
                    msgToSend.setListCards(makeListCardToSend(cards));
                    this.gameData.setUsedCardGreen(gameData.getUsedCardGreen());
                }
                case CARD ->{
                    List<List<Long>> ids = gameData.getPlayers().stream().map(e->gameData.getCards().get(e)).toList();
                    List<List<Card>> cards = gameData.getPlayers().stream().map(e->cardRepository.getCards(gameData.getCards().get(e))).toList();

                    msgToSend.setOwners(gameData.getPlayers());
                    msgToSend.setDataListLong(ids);
                    msgToSend.setListCards(makeListCardToSend(cards));
                    this.gameData.setCards(gameData.getCards());
                }
                case RESOURCES ->{
                    msgToSend.setOwners(gameData.getPlayers());
                    msgToSend.setResources(gameData.getPlayers().stream().map(e->gameData.getResources().get(e)).toList());
                    this.gameData.setResources(gameData.getResources());
                }
                case PLANET ->{
                    msgToSend.setMsg(gameData.getPlanet().stream().map(Enum::toString).toList());
                    this.gameData.setPlanet(gameData.getPlanet());
                }
                case LEVEL ->{
                    msgToSend.setOwners(gameData.getPlayers());
                    msgToSend.setDataLong(gameData.getPlayers().stream().map(e->gameData.getLevel().get(e)).toList());

                    this.gameData.setLevel(gameData.getLevel());
                }
                case OTHERS ->{
                    msgToSend.setMsg(List.of(
                            gameData.getRound(),
                            gameData.getWinningPoints().getWinTemp(), gameData.getWinningPoints().getTemp(),
                            gameData.getWinningPoints().getWinOxygen(), gameData.getWinningPoints().getOxygen(),
                            gameData.getWinningPoints().getWinOcean(), gameData.getWinningPoints().getOcean()
                    ).stream().map(String::valueOf).toList());
                    this.gameData.setRound(gameData.getRound());
                    this.gameData.setWinningPoints(gameData.getWinningPoints());
                }
                case PRIZE -> {
                    msgToSend.setOwners(gameData.getPlayers());

                    List<String> list = new ArrayList<>();

                    for(String s: gameData.getPrize().keySet()){
                        if(gameData.getPrize().get(s) == true){
                            list.add(s);
                        }
                    }

                    msgToSend.setMsg(list);


                    this.gameData.setPrize(gameData.getPrize());
                }
                case TITLES -> {
                    msgToSend.setOwners(gameData.getPlayers());

                    List<String> list = new ArrayList<>();

                    for(String s: gameData.getTitles().keySet()){
                        if(gameData.getTitles().get(s) != null){
                            list.add(s);
                            list.add(gameData.getTitles().get(s));
                        }
                    }
                    msgToSend.setMsg(list);

                    this.gameData.setTitles(gameData.getTitles());
                }
            }

            rabbitTemplate.convertAndSend(queueSUB.getName(), msgToSend);
        }

        return null;
    }

    public void sendIAmIn(MyMessage send){
        MyMessage message = new MyMessage();

        message.setFrom(uniqName);
        message.setMessageType(MessageType.USER_IN);
        message.setMsg(List.of(uniqName));


        //rabbitTemplate.convertAndSend(addressMap.get(Adress.GAMECORE), message);
        rabbitTemplate.convertAndSend(addressMap.get(Adress.PLAYERS), "", message);
    }

    public void sendIAmOut(MyMessage send){
        MyMessage message = new MyMessage();

        message.setFrom(uniqName);
        message.setMessageType(MessageType.USER_OUT);
        message.setMsg(List.of(uniqName));

        //rabbitTemplate.convertAndSend(addressMap.get(Adress.GAMECORE),  message);
        rabbitTemplate.convertAndSend(addressMap.get(Adress.PLAYERS), "", message);
    }

    public void sendLobbyQuit(){
        MyMessage message = new MyMessage();

        message.setFrom(uniqName);
        message.setAbout(uniqName);
        message.setMessageType(MessageType.LOBBY_QUIT);
        message.setMsg(List.of(uniqName));

        //rabbitTemplate.convertAndSend(addressMap.get(Adress.GAMECORE),  message);
        rabbitTemplate.convertAndSend(addressMap.get(Adress.PLAYERS), "", message);
    }

    public void userActiveSend(MyMessage send){
        MyMessage message = new MyMessage();

        message.setFrom(uniqName);
        message.setMessageType(MessageType.USER_ACTIVE);
        message.setMsg(List.of(uniqName));


        rabbitTemplate.convertAndSend(addressMap.get(Adress.PLAYERS), "", message);
        //rabbitTemplate.convertAndSend(addressMap.get(Adress.GAMECORE), message);
    }

    private void userInactiveSend(MyMessage send){
        MyMessage message = new MyMessage();

        message.setFrom(uniqName);
        message.setMessageType(MessageType.USER_INACTIVE);
        message.setMsg(List.of(uniqName));

        rabbitTemplate.convertAndSend(addressMap.get(Adress.PLAYERS), "", message);
        //rabbitTemplate.convertAndSend(addressMap.get(Adress.GAMECORE), message);
    }

    public void sendGameCreated(String gameURL){
        MyMessage myMessage = new MyMessage();
        myMessage.setMessageType(MessageType.GAME_CREATED);
        myMessage.setMsg(List.of(gameURL));

        sendToPlayers(myMessage);
    }

    public MyMessage clock(MyMessage message){
        MyMessage myMessage = new MyMessage();
        myMessage.setFrom("You");
        myMessage.setMessageType(MessageType.CLOCK);
        myMessage.setMsg(List.of(message.getMsg().get(0), message.getMsg().get(1)));

        //return myMessage;
        return myMessage;
    }

    private MyMessage save(MyMessage msg){
        switch (msg.getMessageType()){
            case MAIN_CARDS_SAVE -> {
                gameData.getMainCards().put(msg.getMsg().get(0), msg.getCards().get(0).getIndex());
                return msg;
            }
            case CARDS -> {
                gameData.getCards().put(uniqName, msg.getCards().stream().map(CardToSend::getIndex).toList());
                return msg;
            }
            case RESOURCES -> {
                gameData.getResources().put(msg.getAbout(), msg.getResources().get(msg.getOwners().indexOf(msg.getAbout())));
                return msg;
            }
            case USE_CARD -> {
                switch (msg.getCards().get(0).getTypeCard()){
                    case BLUE -> {
                        List<Long> ids = gameData.getUsedCardBlue().get(msg.getAbout());
                        ids.add(msg.getCards().get(0).getIndex());
                        gameData.getUsedCardBlue().put(msg.getAbout(), ids);
                    }
                    case GREEN -> {
                        List<Long> ids = gameData.getUsedCardGreen().get(msg.getAbout());
                        ids.add(msg.getCards().get(0).getIndex());
                        gameData.getUsedCardGreen().put(msg.getAbout(), ids);
                    }
                    case RED -> {
                        List<Long> ids = gameData.getUsedCardRed().get(msg.getAbout());
                        ids.add(msg.getCards().get(0).getIndex());
                        gameData.getUsedCardRed().put(msg.getAbout(), ids);
                    }
                }
                if(msg.getAbout().equals(uniqName)){
                    List<Long> ids = new ArrayList<>();
                    for(Long l: gameData.getCards().get(uniqName)){
                        if(l != msg.getCards().get(0).getIndex()){
                            ids.add(l);
                        }
                    }

                    gameData.getCards().put(msg.getAbout(), ids);
                }
                return msg;
            }
            case OTHERS -> {
                gameData.setRound(Integer.parseInt(msg.getMsg().get(0)));
                gameData.setWinningPoints(new WinningPoints(
                        Integer.parseInt(msg.getMsg().get(2)), Integer.parseInt(msg.getMsg().get(6)), Integer.parseInt(msg.getMsg().get(4)),
                        Integer.parseInt(msg.getMsg().get(1)), Integer.parseInt(msg.getMsg().get(5)), Integer.parseInt(msg.getMsg().get(3))
                ));

                return msg;
            }
            case TITLE -> {
                gameData.getTitles().put(msg.getMsg().get(0), msg.getAbout());
                return msg;
            }
            case PRIZE ->{
                gameData.getPrize().put(msg.getMsg().get(0), true);
                return msg;
            }
        }

        return null;
    }

    private MyMessage dontTouchNotMe(MyMessage message){
        if(message.getAbout().equals(uniqName)){
            MyMessage newM = new MyMessage();
            newM.setAbout(uniqName);
            newM.setMessageType(MessageType.PING);
            return newM;
        }

        return message;
    }

    @Override
    protected void notFoundDetailSend(MyMessage m) {
        MyMessage message = new MyMessage();

        message.setFrom(uniqName);
        message.setMessageType(MessageType.NULL);
        message.setMsg(List.of("null"));

        rabbitTemplate.convertAndSend(addressMap.get(Adress.PLAYERS), "", message);
    }

    @Override
    protected MyMessage notFoundDetailReceive(MyMessage m) {
        return m;
    }

    private void sendToGame(MyMessage m){
        m.setFrom(uniqName);
        m.setAbout(uniqName);
        rabbitTemplate.convertAndSend(addressMap.get(Adress.GAMECORE), "help", m);
    }
}
