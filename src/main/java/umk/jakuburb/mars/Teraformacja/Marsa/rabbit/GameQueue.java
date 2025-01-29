package umk.jakuburb.mars.Teraformacja.Marsa.rabbit;

import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.core.parameters.P;
import umk.jakuburb.mars.Teraformacja.Marsa.game.ChatRecord;
import umk.jakuburb.mars.Teraformacja.Marsa.game.GameData;
import umk.jakuburb.mars.Teraformacja.Marsa.utils.Timerable;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class GameQueue extends GameCoreQueue implements Timerable {

    private HashMap<MessageType, Function<MyMessage, Boolean>> checkAndSave;
    private GameData gameDate;

    public static final String NAME = "GameQueue";
    private String playersExchange;
    private final int RoundTimeSetting = 60;
    private int roundTime = 60;
    private int whoPlay = 0;


    //TODO: o bakup nie sie ma co sie martwic bo przeciez dziala fifo
    //wiec jak beda dwa taski: backup, nowa wiadomosc
    //to jak najpierw bedzie backup to zrobi backup i potem doda nowa wiadomosc,
    //jesli na odwrot to najpierw da nowa wiadomosc, a potem backup, który bedzie juz z ta nowa wiadomoscia

    public GameQueue(String uniqName,String playersExchange, RabbitAdmin rabbitAdmin, RabbitTemplate rabbitTemplate) {
        super(uniqName, rabbitAdmin, rabbitTemplate);

        gameDate = new GameData();
        this.playersExchange = playersExchange;
        checkAndSave = new HashMap<>();

        addReceiveFunction(MessageType.RECOVER, this::recover);

        addReceiveFunction(MessageType.MESSAGE_SEND, this::monitorUserMove);
        checkAndSave.put(MessageType.MESSAGE_SEND, this::messageSendCAS);

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

    public boolean messageSendCAS(MyMessage message){
        ChatRecord chatRecord = new ChatRecord(message.getFrom(), message.getMsg().get(0));
        gameDate.getChat().add(chatRecord);

        return true;
    }

    @Override
    protected MyMessage notFoundTypeReceive(MyMessage m) {

        return null;
    }

    @Override
    public void doThing() {
        MyMessage myMessage = new MyMessage();
        myMessage.setFrom("Game");
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

    public boolean checkAndSaveDefault(MyMessage message){
        return false;
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
        boolean check = checkAndSave.getOrDefault(message.getMessageType(), this::checkAndSaveDefault).apply(message);

        //przekazuje informacje
        if(check){
            rabbitTemplate.convertAndSend(playersExchange, "", message);
        }else{
            String from = message.getFrom();
            String addressQueue = from + CoreQueue.PRESUB_BEFORE_NAME;

            MyMessage myMessage = new MyMessage();
            myMessage.setFrom(NAME);
            myMessage.setMessageType(MessageType.ERROR);

            rabbitTemplate.convertAndSend(addressQueue, myMessage);
        }
    }
}
