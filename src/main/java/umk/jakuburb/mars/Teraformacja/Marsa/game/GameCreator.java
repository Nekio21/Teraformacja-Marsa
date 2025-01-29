package umk.jakuburb.mars.Teraformacja.Marsa.game;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import umk.jakuburb.mars.Teraformacja.Marsa.database.entity.Player;
import umk.jakuburb.mars.Teraformacja.Marsa.database.repository.GameRepository;
import umk.jakuburb.mars.Teraformacja.Marsa.rabbit.GameQueue;
import umk.jakuburb.mars.Teraformacja.Marsa.utils.NeedURL;
import umk.jakuburb.mars.Teraformacja.Marsa.database.entity.Game;
import umk.jakuburb.mars.Teraformacja.Marsa.utils.Timer;

import java.util.ArrayList;
import java.util.List;

@Component
public class GameCreator extends Creator{

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Timer timer;

    public static final String EXCHANGE_USERS_NAME_START = "Users";
    public static final String EXCHANGE_GAME_NAME_START = "Game";


    public GameCreator(){
        super();
    }

    @Override
    public String create(Player player) throws Exception {
        Game game = new Game();

        game.setCountRounds(1);

        ArrayList<Player> arrayList = new ArrayList<>();

        arrayList.add(player);

        game.setPlayers(arrayList);
        game = gameRepository.save(game);

        String URL = setURL(game);

        return URL;
    }

    @Override
    public Object createExchange(String name) {
        FanoutExchange usersExchange = new FanoutExchange(EXCHANGE_USERS_NAME_START + name);
        rabbitAdmin.declareExchange(usersExchange);

        return setupGameQueue(name);
    }

    private GameQueue setupGameQueue(String name){
        GameQueue gameQueue = new GameQueue(EXCHANGE_GAME_NAME_START + name, EXCHANGE_USERS_NAME_START + name, rabbitAdmin, rabbitTemplate);
        timer.subscribe(gameQueue);

        return gameQueue;
    }


    @Override
    protected void save(NeedURL nurl) {
        if(nurl instanceof Game) {
            gameRepository.save((Game)nurl);
        }
    }

    @Override
    protected List<String> getAllURL() {
        return gameRepository.getAllURL();
    }
}
