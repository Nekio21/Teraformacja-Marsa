package umk.jakuburb.mars.Teraformacja.Marsa.controllers;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import umk.jakuburb.mars.Teraformacja.Marsa.database.entity.Game;
import umk.jakuburb.mars.Teraformacja.Marsa.database.entity.Lobby;
import umk.jakuburb.mars.Teraformacja.Marsa.database.entity.Player;
import umk.jakuburb.mars.Teraformacja.Marsa.database.repository.GameRepository;
import umk.jakuburb.mars.Teraformacja.Marsa.database.repository.LobbyRepository;
import umk.jakuburb.mars.Teraformacja.Marsa.database.repository.PlayerRepository;
import umk.jakuburb.mars.Teraformacja.Marsa.game.GameCreator;
import umk.jakuburb.mars.Teraformacja.Marsa.rabbit.Adress;
import umk.jakuburb.mars.Teraformacja.Marsa.rabbit.GameQueue;
import umk.jakuburb.mars.Teraformacja.Marsa.rabbit.MyMessage;
import umk.jakuburb.mars.Teraformacja.Marsa.utils.MySession;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Controller
@RequestMapping("/mars/game")
public class GameController {

    @Autowired
    private LobbyRepository lobbyRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private GameCreator gameCreator;

    @Autowired
    private MySession mySession;

    @GetMapping("/{url}/createGame")
    public String createGame(@PathVariable String url){
        Player host = mySession.getPlayer();
        Optional<Lobby> lobby = lobbyRepository.findByUrl(url);
        String gameURL = "";

        if(lobby.isEmpty()){
            return "redirect:/mars/error";
        }

        if(!host.getId().equals(lobby.get().getHost().getId()) || lobby.get().getPlayers().size() != 3){
            return "redirect:/mars/lobby/" + url;
        }

        try {
            gameURL = gameCreator.create(mySession.getPlayer());
            Optional<Game> game = gameRepository.findByUrl(gameURL);

            if(game.isEmpty()) throw new Exception();

            List<Player> withoutHost = lobby.get().getPlayers().stream().filter(p->!lobby.get().getHost().equals(p)).toList();

            game.get().getPlayers().addAll(withoutHost);

            gameRepository.save(game.get());

        }catch (Exception e) {
            System.out.println("bład[GameController(createGame)]: " + e);
            return "redirect:/mars/error";
        }

        GameQueue gameQueue = (GameQueue) gameCreator.createExchange(gameURL);
        gameQueue.getGameDate().setPlayers(lobby.get().getPlayers().stream().map(Player::getLogin).toList());

        mySession.getPlayerQueue().sendGameCreated("/mars/game/" + gameURL);

        return "redirect:/mars/game/" + gameURL;
    }


    @GetMapping("/{url}")
    public String game(@PathVariable String url, Model model){
        Optional<Game> game = gameRepository.findByUrl(url);
        Player mySessionPlayer = mySession.getPlayer();

        if(game.isEmpty()){
            return "redirect:/mars/error";
        }

        boolean isPlayer = game.get().getPlayers().stream().anyMatch(new Predicate<Player>() {
            @Override
            public boolean test(Player player) {
                return mySessionPlayer.getId().equals(player.getId()) && mySessionPlayer.getLogin().equals(player.getLogin());
            }
        });

        if(isPlayer == false){
            return "redirect:/mars/error";
        }

        mySession.getPlayerQueue().createBinding(new FanoutExchange(GameCreator.EXCHANGE_USERS_NAME_START + game.get().getUrl()));
        mySession.getPlayerQueue().addAddress(Adress.PLAYERS, GameCreator.EXCHANGE_USERS_NAME_START + game.get().getUrl());
        mySession.getPlayerQueue().addAddress(Adress.GAMECORE, GameCreator.EXCHANGE_GAME_NAME_START + game.get().getUrl());
        mySession.getPlayerQueue().sendIAmIn(new MyMessage());
        mySession.getPlayerQueue().recoverSend(new MyMessage());

        model.addAttribute("name", mySessionPlayer.getLogin());
        model.addAttribute("subQueue", mySession.getPlayerQueue().getQueueSubName());
        model.addAttribute("sendQueue", mySession.getPlayerQueue().getQueueSendName());

        return "game";
    }
}
