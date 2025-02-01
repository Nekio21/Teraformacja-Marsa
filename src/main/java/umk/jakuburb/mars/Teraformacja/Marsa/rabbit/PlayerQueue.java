package umk.jakuburb.mars.Teraformacja.Marsa.rabbit;

import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import umk.jakuburb.mars.Teraformacja.Marsa.message.*;
import umk.jakuburb.mars.Teraformacja.Marsa.message.gameData.GameData;
import umk.jakuburb.mars.Teraformacja.Marsa.message.gameData.GameDataCheck;
import umk.jakuburb.mars.Teraformacja.Marsa.message.gameData.GameDataProces;

import java.util.List;

public class PlayerQueue extends CoreQueue{

    //TODO: to chyba srednio bezpieczne by podawac nazwe kolejki co nie ???
    //TODO: spawdzac hedery !!!!!
    //https://stackoverflow.com/questions/31564432/websocket-security

    //TODO: java.net.SocketException: An established connection was aborted by the software in your host machine

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

        addSendFunction(MessageType.MAIN_CARDS, this::mainCardSend);
        addReceiveFunction(MessageType.MAIN_CARDS_SAVE, this::save);
        //addSendFunction(MessageType.RECOVER, this::recoverSend);
        addSendFunction(MessageType.CARDS10, this::sendToGame);

        addReceiveFunction(MessageType.RESOURCES, this::save);
        addReceiveFunction(MessageType.CARDS, this::save);
    }

    private void mainCardSend(MyMessage msg){
        msg.setFrom(uniqName);
        rabbitTemplate.convertAndSend(addressMap.get(Adress.GAMECORE),"help", msg);
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

    public void gameIn(){
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
                    msgToSend.setOwners(gameData.getPlayers());
                    msgToSend.setDataLong(gameData.getPlayers().stream().map(e->gameData.getMainCards().get(e)).toList());
                    this.gameData.setMainCards(gameData.getMainCards());
                }
                case USED_CARDS_BLUE -> {
                    msgToSend.setOwners(gameData.getPlayers());
                    msgToSend.setDataListLong(gameData.getPlayers().stream().map(e->gameData.getUsedCardBlue().get(e)).toList());
                    this.gameData.setUsedCardBlue(gameData.getUsedCardBlue());
                }
                case USED_CARDS_RED ->{
                    msgToSend.setOwners(gameData.getPlayers());
                    msgToSend.setDataListLong(gameData.getPlayers().stream().map(e->gameData.getUsedCardRed().get(e)).toList());
                    this.gameData.setUsedCardRed(gameData.getUsedCardRed());
                }
                case USED_CARDS_GREEN ->{
                    msgToSend.setOwners(gameData.getPlayers());
                    msgToSend.setDataListLong(gameData.getPlayers().stream().map(e->gameData.getUsedCardGreen().get(e)).toList());
                    this.gameData.setUsedCardGreen(gameData.getUsedCardGreen());
                }
                case CARD ->{
                    msgToSend.setOwners(gameData.getPlayers());
                    msgToSend.setDataListLong(gameData.getPlayers().stream().map(e->gameData.getCards().get(e)).toList());
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
                    msgToSend.setMsg(List.of(gameData.getRound(), gameData.getTemp(), gameData.getCo2(), gameData.getOcean()).stream().map(String::valueOf).toList());
                    this.gameData.setRound(gameData.getRound());
                    this.gameData.setTemp(gameData.getTemp());
                    this.gameData.setCo2(gameData.getCo2());
                    this.gameData.setOcean(gameData.getOcean());
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

        //return myMessage;
        return null;
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
        }

        return null;
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
        rabbitTemplate.convertAndSend(addressMap.get(Adress.GAMECORE), "help", m);
    }
}
