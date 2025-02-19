package umk.jakuburb.mars.Teraformacja.Marsa.rabbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import umk.jakuburb.mars.Teraformacja.Marsa.message.gameData.GameData;
import umk.jakuburb.mars.Teraformacja.Marsa.message.MessageType;
import umk.jakuburb.mars.Teraformacja.Marsa.message.MyMessage;

import java.util.HashMap;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class CoreQueue {
    protected RabbitTemplate rabbitTemplate;
    private RabbitAdmin rabbitAdmin;

    protected Queue queueSUB;
    protected Queue queueSEND;
    protected Queue queuePreSUB;

    public static final String PRESUB_BEFORE_NAME = "kolejkaPreSub";
    public static final String SUB_BEFORE_NAME = "kolejkaSub";
    public static final String SEND_BEFORE_NAME = "kolejkaSend";

    private HashMap<MessageType, Consumer<MyMessage>> sendMap;
    private HashMap<MessageType, Function<MyMessage, MyMessage>> receiveMap;

    protected HashMap<Adress, String> addressMap;

    protected GameData gameData;

    protected String uniqName;
    private String queueSubName;
    private String queueSendName;

    //TODO: Moze usuwac uniqname do ostatecznego Receive

    public CoreQueue(
            String uniqName,
            RabbitAdmin rabbitAdmin,
            RabbitTemplate rabbitTemplate
    ){
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitAdmin = rabbitAdmin;

        this.uniqName = uniqName;
        gameData = new GameData();

        sendMap = new HashMap<>();
        receiveMap = new HashMap<>();
        addressMap = new HashMap<>();

        queueInit(uniqName);

        addQueueListener(rabbitAdmin, queueSEND, this::fullSend);
        addQueueListener(rabbitAdmin, queuePreSUB, this::fullReceive);
    }

    protected void addSendFunction(MessageType type, Consumer<MyMessage> fun){
        sendMap.put(type, fun);
    }
    protected void addReceiveFunction(MessageType type, Function<MyMessage, MyMessage> fun){
        receiveMap.put(type, fun);
    }

    public void fullSend(MyMessage message){
        Consumer<MyMessage> fun = sendMap.getOrDefault(message.getMessageType(), this::notFoundDetailSend);
        fun.accept(message);

        System.out.println(message);
    }

    public void fullReceive(MyMessage message){
        Function<MyMessage, MyMessage> fun = receiveMap.getOrDefault(message.getMessageType(), this::notFoundDetailReceive);

        MyMessage result = fun.apply(message);

        if(result != null) {
            rabbitTemplate.convertAndSend(queueSUB.getName(), result);
            System.out.println(result);
            System.out.println(result.toString());
        }
    }

    protected abstract void notFoundDetailSend(MyMessage m);

    protected abstract MyMessage notFoundDetailReceive(MyMessage m);

    private void queueInit(String uniqName){
        Random random = new Random();

        queueSubName = SUB_BEFORE_NAME + uniqName + (100000 + random.nextInt(100000));
        queueSendName = SEND_BEFORE_NAME + uniqName + (100000 + random.nextInt(100000));

        queueSUB = new Queue(queueSubName);
        queuePreSUB = new Queue(PRESUB_BEFORE_NAME + uniqName);
        queueSEND = new Queue(queueSendName);

        rabbitAdmin.declareQueue(queueSUB);
        rabbitAdmin.declareQueue(queuePreSUB);
        rabbitAdmin.declareQueue(queueSEND);
    }

    public void createBinding(FanoutExchange exchange){
        Binding binding = BindingBuilder.bind(queuePreSUB).to(exchange);
        rabbitAdmin.declareBinding(binding);
    }

    public void deleteBinding(FanoutExchange exchange){
        Binding binding = BindingBuilder.bind(queuePreSUB).to(exchange);
        rabbitAdmin.removeBinding(binding);
    }

    public void createBinding(DirectExchange exchange, String key){
        Binding binding = BindingBuilder.bind(queuePreSUB).to(exchange).with(key);
        rabbitAdmin.declareBinding(binding);
    }

    public void addAddress(Adress adress, String name){
        addressMap.put(adress, name);
    }

    private void addQueueListener(RabbitAdmin rabbitAdmin, Queue queue, Consumer<MyMessage> func){
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
                System.out.println("Bład[CoreQueue(addQueueListener)]: " + e);
            }
        });

        listener.start();
    }

    protected MyMessage dontTouch(MyMessage message){
        return message;
    }

    protected void sendToPlayers(MyMessage message){
        message.setFrom(uniqName);
        rabbitTemplate.convertAndSend(addressMap.get(Adress.PLAYERS), "", message);
    }

    public String getUniqName() {
        return uniqName;
    }

    public void setUniqName(String uniqName) {
        this.uniqName = uniqName;
    }

    public String getQueueSubName() {
        return queueSubName;
    }

    public void setQueueSubName(String queueSubName) {
        this.queueSubName = queueSubName;
    }

    public String getQueueSendName() {
        return queueSendName;
    }

    public void setQueueSendName(String queueSendName) {
        this.queueSendName = queueSendName;
    }
}
