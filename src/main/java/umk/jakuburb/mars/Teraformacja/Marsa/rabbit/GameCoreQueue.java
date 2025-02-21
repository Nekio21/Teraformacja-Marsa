package umk.jakuburb.mars.Teraformacja.Marsa.rabbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import umk.jakuburb.mars.Teraformacja.Marsa.message.MessageType;
import umk.jakuburb.mars.Teraformacja.Marsa.message.MyMessage;

import java.util.HashMap;
import java.util.function.Consumer;

public abstract class GameCoreQueue {

    protected RabbitTemplate rabbitTemplate;
    private RabbitAdmin rabbitAdmin;
    private DirectExchange directExchange;
    protected Queue helpPlayers;
    protected String uniqName;

    private HashMap<MessageType, Consumer<MyMessage>> receiveMap;
    public static final String CLOCK_EXCHANGE_NAME = "games.clock";

    public enum GameState{
        WAITING,
        CHOSE_CARDS,
        BEFORE_ROUND,
        ROUND,
        AFTER_ROUND,
        END_GAME
    }

    public enum UserState{
        WAITING,
        CHOSE_MAIN_CARD,
        CHOSE_CARD,
        NO_MOVE,
        FIRST_MOVE,
        SECOND_MOVE,
        PASS,
    }

    public GameCoreQueue(
            String uniqName,
            RabbitAdmin rabbitAdmin,
            RabbitTemplate rabbitTemplate
    ){
        this.uniqName = uniqName;
        this.rabbitAdmin = rabbitAdmin;
        this.rabbitTemplate = rabbitTemplate;

        receiveMap = new HashMap<>();

        init();
    }

    private void init(){
        String helpQueueName = "help.players." + uniqName;

        helpPlayers = new Queue(helpQueueName);

        directExchange = new DirectExchange(uniqName);
        rabbitAdmin.declareQueue(helpPlayers);
        rabbitAdmin.declareExchange(directExchange);

        Binding binding2 = BindingBuilder.bind(helpPlayers).to(directExchange).with("help");

        rabbitAdmin.declareBinding(binding2);

        addQueueListener(helpPlayers, this::receive);
    }

    protected void addReceiveFunction(MessageType type, Consumer<MyMessage> fun){
        receiveMap.put(type, fun);
    }

    protected void monitorUserMove(){

    }

    private void receive(MyMessage message){
        Consumer<MyMessage> fun = receiveMap.getOrDefault(message.getMessageType(), this::notFoundTypeReceive);
        fun.accept(message);

        System.out.println(message);
    }

    protected abstract MyMessage notFoundTypeReceive(MyMessage m);

    private void addQueueListener(Queue queue, Consumer<MyMessage> func){
        SimpleMessageListenerContainer listener = new SimpleMessageListenerContainer(
                rabbitAdmin.getRabbitTemplate().getConnectionFactory()
        );

        listener.addQueues(queue);

        listener.setMessageListener(message -> {
            System.out.println(message);

            try {
                byte[] m = message.getBody();
                ObjectMapper mapper = new ObjectMapper();
                MyMessage myMessageLite = mapper.readValue(new String(m), MyMessage.class);

                func.accept(myMessageLite);
            }catch(Exception e){
                System.out.println("Bład[GameCoreQueue(addQueueListener)]: " + e);
            }
        });

        listener.start();
    }
}
