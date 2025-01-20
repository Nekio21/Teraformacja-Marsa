package umk.jakuburb.mars.Teraformacja.Marsa.rabbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

import java.util.function.Consumer;

public abstract class GameCoreQueue {

    private RabbitTemplate rabbitTemplate;
    private RabbitAdmin rabbitAdmin;

    private DirectExchange directExchange;
    protected Queue clock;
    protected Queue helpPlayers;

    private String uniqName;

    public static final String CLOCK_EXCHANGE_NAME = "games.clock";

    public GameCoreQueue(
            String uniqName,
            RabbitAdmin rabbitAdmin,
            RabbitTemplate rabbitTemplate
    ){
        this.uniqName = uniqName;
        this.rabbitAdmin = rabbitAdmin;
        this.rabbitTemplate = rabbitTemplate;

        init();
    }

    private void init(){
        String clockQueueName = "clock." + uniqName;
        String helpQueueName = "help.players." + uniqName;

        clock = new Queue(clockQueueName);
        helpPlayers = new Queue(helpQueueName);

        directExchange = new DirectExchange(uniqName);
        FanoutExchange fanoutExchange = new FanoutExchange(CLOCK_EXCHANGE_NAME);

        rabbitAdmin.declareQueue(clock);
        rabbitAdmin.declareQueue(helpPlayers);
        rabbitAdmin.declareExchange(directExchange);
        rabbitAdmin.declareExchange(fanoutExchange);

        Binding binding1 = BindingBuilder.bind(clock).to(fanoutExchange);
        Binding binding2 = BindingBuilder.bind(helpPlayers).to(directExchange).with("help");

        rabbitAdmin.declareBinding(binding1);
        rabbitAdmin.declareBinding(binding2);
    }

    protected void addQueueListener(RabbitAdmin rabbitAdmin, Queue queue, Consumer<MyMessage> func){
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
}
