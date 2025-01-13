package umk.jakuburb.mars.Teraformacja.Marsa.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import umk.jakuburb.mars.Teraformacja.Marsa.database.entity.Player;
import umk.jakuburb.mars.Teraformacja.Marsa.database.repository.PlayerRepository;
import umk.jakuburb.mars.Teraformacja.Marsa.rabbit.PlayerQueue;

@Component
@Scope(scopeName = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class MySession {

    private PlayerQueue playerQueue;

    @Autowired
    private PlayerRepository playerRepository;

    public MySession(){

    }

    public void clear(){
        //TODO: wyczycznie wszystkich pol

        playerQueue = null;
    }

    public PlayerQueue getPlayerQueue() {
        return playerQueue;
    }

    public void setPlayerQueue(PlayerQueue playerQueue) {
        this.playerQueue = playerQueue;
    }

    public Player getPlayer(){
        UserDetails playerInfo = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Player player = playerRepository.findByLogin(playerInfo.getUsername())
                .orElseThrow(()->null);

        return player;
    }
}
