package umk.jakuburb.mars.Teraformacja.Marsa.utils;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import umk.jakuburb.mars.Teraformacja.Marsa.rabbit.PlayerQueue;

@Component
@Scope(scopeName = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class MySession {

    private PlayerQueue playerQueue;

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
}
