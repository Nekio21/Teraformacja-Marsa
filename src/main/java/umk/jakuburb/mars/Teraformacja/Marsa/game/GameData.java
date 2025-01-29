package umk.jakuburb.mars.Teraformacja.Marsa.game;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class GameData implements Serializable {

    @JsonProperty("chatMessage")
    private List<ChatRecord> chat;

    private List<String> players;

    public GameData(){
        chat = new ArrayList<>();
        players = new ArrayList<>();
    }

    public static List<GameDataCheck> check(GameData gd1, GameData gd2){
        List<GameDataCheck> list = new ArrayList<>();

        if(!Arrays.deepEquals(gd1.getChat().toArray(), gd2.getChat().toArray())){
            list.add(GameDataCheck.CHAT);
        }

        if(!Arrays.deepEquals(gd1.getPlayers().toArray(), gd2.getPlayers().toArray())){
            list.add(GameDataCheck.PLAYERS);
        }

        return list;
    }

    public List<ChatRecord> getChat() {
        return chat;
    }

    public void setChat(List<ChatRecord> chat) {
        this.chat = chat;
    }

    public List<String> getPlayers() {
        return players;
    }

    public void setPlayers(List<String> players) {
        this.players = players;
    }
}
