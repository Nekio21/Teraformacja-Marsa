package umk.jakuburb.mars.Teraformacja.Marsa.rabbit;

import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import umk.jakuburb.mars.Teraformacja.Marsa.game.ChatRecord;
import umk.jakuburb.mars.Teraformacja.Marsa.game.GameData;
import umk.jakuburb.mars.Teraformacja.Marsa.game.GameDataCheck;

import java.util.List;

public class PlayerQueue extends CoreQueue{

    //TODO: to chyba srednio bezpieczne by podawac nazwe kolejki co nie ???
    //TODO: spawdzac hedery !!!!!
    //https://stackoverflow.com/questions/31564432/websocket-security

    public PlayerQueue(String uniqName, RabbitAdmin rabbitAdmin, RabbitTemplate rabbitTemplate){
        super(uniqName, rabbitAdmin, rabbitTemplate);

        addSendFunction(MessageType.USER_IN, this::sendIAmIn);
        addSendFunction(MessageType.USER_OUT, this::sendIAmOut);

        addReceiveFunction(MessageType.USER_IN, this::dontTouch);
        addReceiveFunction(MessageType.USER_OUT, this::dontTouch);

        addSendFunction(MessageType.USER_ACTIVE, this::userActiveSend);
        addSendFunction(MessageType.USER_INACTIVE, this::userInactiveSend);

        addReceiveFunction(MessageType.USER_ACTIVE, this::dontTouch);
        addReceiveFunction(MessageType.USER_INACTIVE, this::dontTouch);

        addReceiveFunction(MessageType.GAME_CREATED, this::dontTouch);

        addSendFunction(MessageType.MESSAGE_SEND, this::messageSend);
        addReceiveFunction(MessageType.MESSAGE_SEND, this::messageReceive);

        addReceiveFunction(MessageType.CLOCK, this::clock);
        addReceiveFunction(MessageType.RECOVER, this::recover);

        addSendFunction(MessageType.RECOVER, this::recoverSend);
    }


    private void messageSend(MyMessage send){
        MyMessage message = new MyMessage();

        message.setFrom(uniqName);
        message.setMessageType(MessageType.MESSAGE_SEND);
        message.setMsg(List.of(send.getMsg().get(0), uniqName));

        rabbitTemplate.convertAndSend(addressMap.get(Adress.GAMECORE),"help", message);
        //rabbitTemplate.convertAndSend(addressMap.get(Adress.PLAYERS), "", message);
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

    public void recoverSend(MyMessage msg){
        msg.setFrom(uniqName);
        msg.setMessageType(MessageType.RECOVER);
        msg.setMsg(List.of(uniqName));

        rabbitTemplate.convertAndSend(addressMap.get(Adress.GAMECORE),"help", msg);
    }

    private MyMessage recover(MyMessage msg){
        GameData gameData = msg.getGameData();

        List<GameDataCheck> check = GameData.check(gameData, this.gameData);

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

    private void sendIAmOut(MyMessage send){
        MyMessage message = new MyMessage();

        message.setFrom(uniqName);
        message.setMessageType(MessageType.USER_OUT);
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

        if(message.getMsg().get(0).equals(uniqName)){
            myMessage.setMsg(List.of("true", message.getMsg().get(1)));
        }else{
            myMessage.setMsg(List.of("false", message.getMsg().get(1)));
        }

        return myMessage;
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
}
