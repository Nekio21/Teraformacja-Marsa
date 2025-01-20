package umk.jakuburb.mars.Teraformacja.Marsa.rabbit;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import umk.jakuburb.mars.Teraformacja.Marsa.game.GameCore;

public class GameQueue extends GameCoreQueue{

    private GameCore gameCore;
    private String playersExchange;

    public GameQueue(String uniqName,String playersExchange, RabbitAdmin rabbitAdmin, RabbitTemplate rabbitTemplate) {
        super(uniqName, rabbitAdmin, rabbitTemplate);

        gameCore = new GameCore();
        this.playersExchange = playersExchange;

        addQueueListener(rabbitAdmin, clock, this::clock);
        addQueueListener(rabbitAdmin, helpPlayers, this::help);
    }

    public void clock(MyMessage message){
        System.out.println("zegarek :)");
    }

    public void help(MyMessage message){
        System.out.println("help :)");
    }
}
