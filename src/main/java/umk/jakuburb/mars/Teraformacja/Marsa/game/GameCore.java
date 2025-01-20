package umk.jakuburb.mars.Teraformacja.Marsa.game;

import java.util.ArrayList;
import java.util.List;

public class GameCore {
    private List<ChatRecord> chat;

    public GameCore(){
        chat = new ArrayList<>();
    }

    public List<ChatRecord> getChat() {
        return chat;
    }

    public void setChat(List<ChatRecord> chat) {
        this.chat = chat;
    }
}
