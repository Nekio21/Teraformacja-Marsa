package umk.jakuburb.mars.Teraformacja.Marsa.rabbit;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SystemQueue {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private final static String NAME = "System";

    public SystemQueue(){

    }

    @Scheduled(fixedDelay = 1000)
    public void clockMethod(){
        rabbitTemplate.convertAndSend(GameCoreQueue.CLOCK_EXCHANGE_NAME, "", new MyMessage(MessageType.CLOCK, NAME, List.of("true"), ""));
    }
}
