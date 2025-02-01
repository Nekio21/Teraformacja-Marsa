package umk.jakuburb.mars.Teraformacja.Marsa.controllers;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import umk.jakuburb.mars.Teraformacja.Marsa.config.SecurityConfig;
import umk.jakuburb.mars.Teraformacja.Marsa.database.entity.Lobby;
import umk.jakuburb.mars.Teraformacja.Marsa.database.entity.Player;
import umk.jakuburb.mars.Teraformacja.Marsa.database.repository.LobbyRepository;
import umk.jakuburb.mars.Teraformacja.Marsa.database.repository.PlayerRepository;
import umk.jakuburb.mars.Teraformacja.Marsa.game.GameLobbyCreator;
import umk.jakuburb.mars.Teraformacja.Marsa.rabbit.Adress;
import umk.jakuburb.mars.Teraformacja.Marsa.message.MyMessage;
import umk.jakuburb.mars.Teraformacja.Marsa.utils.MySession;

import java.util.Optional;
import java.util.function.Predicate;

@Controller
@RequestMapping("/mars/lobby")
public class WebLobby {

    //TODO: potem stworz sersy i tam ma byc cala logika

    @Autowired
    private GameLobbyCreator gameLobbyCreator;

    @Autowired
    private MySession mySession;

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private HttpSecurity http;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private LobbyRepository lobbyRepository;

    @GetMapping("/{url}")
    public String lobby(@PathVariable String url, Model model){

        Optional<Lobby> lobby = lobbyRepository.findByUrl(url);

        if(lobby.isEmpty()){
            return "redirect:/mars/error";
        }

        Player player2 = mySession.getPlayer();

        boolean isPlayer = lobby.get().getPlayers().stream().anyMatch(new Predicate<Player>() {
            @Override
            public boolean test(Player player) {
                return player2.getId().equals(player.getId()) && player2.getLogin().equals(player.getLogin());
            }
        });

        if(isPlayer == false){
            return "redirect:/mars/error";
        }

        model.addAttribute("nickname", player2.getLogin());
        model.addAttribute("subQueue", mySession.getPlayerQueue().getQueueSubName());
        model.addAttribute("sendQueue", mySession.getPlayerQueue().getQueueSendName());
        model.addAttribute("host", lobby.get().getHost().getLogin());
        model.addAttribute("ishost", lobby.get().getHost().getLogin().equals(player2.getLogin()));
        model.addAttribute("code", lobby.get().getCode());
        model.addAttribute("players", lobby.get().getPlayers().stream().map(Player::getLogin).toList());
        model.addAttribute("link", "/mars/game/" + url + "/createGame");


        mySession.getPlayerQueue().createBinding(new FanoutExchange("Lobby" + lobby.get().getUrl()));
        mySession.getPlayerQueue().addAddress(Adress.PLAYERS, "Lobby" + lobby.get().getUrl());
        mySession.getPlayerQueue().sendIAmIn(new MyMessage());

        return "lobby";
    }

    @GetMapping("/create")
    public String create() throws Exception {
        String URL = "";

        try {
            URL = gameLobbyCreator.create(mySession.getPlayer());
        }catch (Exception e) {
            System.out.println("bład[WebLobby(create)]: " + e);

            return "redirect:/mars/error";
        }

        gameLobbyCreator.createExchange("Lobby" + URL);

        return "redirect:/mars/lobby/" + URL;
    }

    @PostMapping("/join")
    public String join(String code){
        Optional<Lobby> lobby = lobbyRepository.findByCode(code);

        if(lobby.isEmpty()){
            //TODO: dodaj ? ktory bedzie mowil ze kod jest zly i moze 3 zle kody i zawiesic na jakis czas usera nie wiem :D
            return "redirect:/mars/home";
        }

        Player player = mySession.getPlayer();

        if(lobby.get().getPlayers().stream().noneMatch(e->player.getLogin().equals(e.getLogin()))) {
            lobby.get().getPlayers().add(player);
            lobbyRepository.save(lobby.get());
        }

        String URL = lobby.get().getUrl();

        return  "redirect:/mars/lobby/" + URL;
    }
}
