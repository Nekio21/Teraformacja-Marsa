package umk.jakuburb.mars.Teraformacja.Marsa.game;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import umk.jakuburb.mars.Teraformacja.Marsa.database.entity.Lobby;
import umk.jakuburb.mars.Teraformacja.Marsa.database.entity.Player;
import umk.jakuburb.mars.Teraformacja.Marsa.database.repository.LobbyRepository;
import umk.jakuburb.mars.Teraformacja.Marsa.exception.CodeSpaceException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
public class GameLobby {

    @Autowired
    private LobbyRepository lobbyRepository;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    private char[] letter = new char[62];
    private Random random;

    private List<String> allCode;
    private List<String> allURL;

    public GameLobby(){
        random = new Random();
        initLetter();
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


    public void createExchange(String name){
        FanoutExchange fanoutExchange = new FanoutExchange(name);
        rabbitAdmin.declareExchange(fanoutExchange);
    }
    private Lobby setCode(Lobby lobby) throws Exception {
        String s = findCode();

        if(s == null){
            throw new CodeSpaceException();
        }

        lobby.setCode(s);
        return lobbyRepository.save(lobby);
    }

    private String setURL(Lobby lobby) throws Exception{
        String URL = findURL();

        if(URL == null){
            throw new CodeSpaceException();
        }

        lobby.setUrl(URL);
        lobbyRepository.save(lobby);

        return URL;
    }

    private String findURL(){
        for(int i=0;i<3;i++) {
            String url = UUID.randomUUID().toString();
            boolean check = checkURL(url);

            if (check) {
                return url;
            }
        }

        return null;
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

    private boolean checkURL(String URL){
        allURL = lobbyRepository.getAllURL();

        return allURL.stream().noneMatch(URL::equals);
    }

    private void initLetter(){
        for(int i=0; i<10; i++){
            letter[i] = (char)(48+i);
        }

        for(int i=10;i<36;i++){
            letter[i] = (char)(i+55);
        }

        for(int i=36;i<62;i++){
            letter[i] = (char)(i+61);
        }
    }
}