package umk.jakuburb.mars.Teraformacja.Marsa.game;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import umk.jakuburb.mars.Teraformacja.Marsa.database.entity.Lobby;
import umk.jakuburb.mars.Teraformacja.Marsa.database.entity.Player;
import umk.jakuburb.mars.Teraformacja.Marsa.database.repository.LobbyRepository;
import umk.jakuburb.mars.Teraformacja.Marsa.exception.CodeSpaceException;
import umk.jakuburb.mars.Teraformacja.Marsa.utils.NeedURL;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
public class GameLobbyCreator extends Creator{

    @Autowired
    private LobbyRepository lobbyRepository;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    private List<String> allCode;

    public GameLobbyCreator(){
        super();
    }

    public String create(Player player) throws Exception {
        Lobby lobby = new Lobby();

        lobby.setPrivate(true);
        lobby.setHost(player);

        ArrayList<Player> arrayList = new ArrayList<>();

        arrayList.add(player);

        lobby.setPlayers(arrayList);
        lobby = lobbyRepository.save(lobby);

        lobby = setCode(lobby);
        String URL = setURL(lobby);

        return URL;
    }


    public Object createExchange(String name){
        FanoutExchange fanoutExchange = new FanoutExchange(name);
        rabbitAdmin.declareExchange(fanoutExchange);

        return null;
    }

    @Override
    protected void save(NeedURL nurl) {
        if(nurl instanceof Lobby){
            lobbyRepository.save((Lobby)nurl);
        }
    }

    @Override
    protected List<String> getAllURL() {
        return lobbyRepository.getAllURL();
    }

    private Lobby setCode(Lobby lobby) throws Exception {
        String s = findCode();

        if(s == null){
            throw new CodeSpaceException();
        }

        lobby.setCode(s);
        return lobbyRepository.save(lobby);
    }

    private String findCode(){
        for(int i=0;i<3;i++) {
            String c = createCode();
            boolean check = checkCode(c);

            if (check) {
                return c;
            }
        }

        return null;
    }
    private String createCode(){
        String code = "";

        String letter1 = String.valueOf(letter[random.nextInt(61)]);
        String letter2 = String.valueOf(letter[random.nextInt(61)]);
        String letter3 = String.valueOf(letter[random.nextInt(61)]);
        String letter4 = String.valueOf(letter[random.nextInt(61)]);
        String letter5 = String.valueOf(letter[random.nextInt(61)]);

        code = letter1 + letter2 + letter3 + letter4 + letter5;

        return code;
    }

    private boolean checkCode(String code){
        allCode = lobbyRepository.getAllCode();

        return allCode.stream().noneMatch(code::equals);
    }
}