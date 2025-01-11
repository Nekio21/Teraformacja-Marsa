package umk.jakuburb.mars.Teraformacja.Marsa.rabbit;

import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;

public class PlayerQueue extends CoreQueue{

    //TODO: to chyba srednio bezpieczne by podawac nazwe kolejki co nie ???
    //https://stackoverflow.com/questions/31564432/websocket-security

    public PlayerQueue(String uniqName, RabbitAdmin rabbitAdmin, RabbitTemplate rabbitTemplate){
        super(uniqName, rabbitAdmin, rabbitTemplate);

        addSendFunction(MessageType.USER_IN, this::sendIAmIn);
        addSendFunction(MessageType.USER_OUT, this::sendIAmOut);

        addReceiveFunction(MessageType.USER_IN, this::receiveSbIn);
        addReceiveFunction(MessageType.USER_OUT, this::receiveSbOut);

        addSendFunction(MessageType.USER_ACTIVE, this::userActiveSend);
        addSendFunction(MessageType.USER_INACTIVE, this::userInactiveSend);

        addReceiveFunction(MessageType.USER_ACTIVE, this::userActiveReceive);
        addReceiveFunction(MessageType.USER_INACTIVE, this::userInactiveReceive);
    }


    public void sendIAmIn(MyMessage send){
        MyMessage message = new MyMessage();

        message.setFrom(uniqName);
        message.setMessageType(MessageType.USER_IN);
        message.setMsg(List.of(uniqName));


        rabbitTemplate.convertAndSend(addressMap.get(Adress.PLAYERS), "", message);
    }

    private void sendIAmOut(MyMessage send){
        MyMessage message = new MyMessage();

        message.setFrom(uniqName);
        message.setMessageType(MessageType.USER_OUT);
        message.setMsg(List.of(uniqName));

        rabbitTemplate.convertAndSend(addressMap.get(Adress.PLAYERS), "", message);
    }

    private MyMessage receiveSbIn(MyMessage receive){
        return receive;
    }

    private MyMessage receiveSbOut(MyMessage receive){
        return receive;
    }

    public void userActiveSend(MyMessage send){
        MyMessage message = new MyMessage();

        message.setFrom(uniqName);
        message.setMessageType(MessageType.USER_ACTIVE);
        message.setMsg(List.of(uniqName));


        rabbitTemplate.convertAndSend(addressMap.get(Adress.PLAYERS), "", message);
    }

    private void userInactiveSend(MyMessage send){
        MyMessage message = new MyMessage();

        message.setFrom(uniqName);
        message.setMessageType(MessageType.USER_INACTIVE);
        message.setMsg(List.of(uniqName));

        rabbitTemplate.convertAndSend(addressMap.get(Adress.PLAYERS), "", message);
    }

    private MyMessage userActiveReceive(MyMessage receive){
        return receive;
    }

    private MyMessage userInactiveReceive(MyMessage receive){
        return receive;
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
