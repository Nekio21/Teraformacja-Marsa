package umk.jakuburb.mars.Teraformacja.Marsa.rabbit;

import com.fasterxml.jackson.annotation.JsonProperty;
import umk.jakuburb.mars.Teraformacja.Marsa.game.ChatRecord;
import umk.jakuburb.mars.Teraformacja.Marsa.game.GameData;
import umk.jakuburb.mars.Teraformacja.Marsa.game.GameDataCheck;

import java.io.Serializable;
import java.util.List;

public class MyMessage implements Serializable {

    @JsonProperty("messageType")
    private MessageType messageType;

    @JsonProperty("recoverType")
    private GameDataCheck recoverType;

    @JsonProperty("from")
    private String from;

    @JsonProperty("msg")
    private List<String> msg;

    @JsonProperty("lobby")
    private String lobby;

    @JsonProperty("data")
    private GameData gameData;

    @JsonProperty("chatRecord")
    private ChatRecord chatRecord;

    @JsonProperty("chatMessage")
    private List<ChatRecord> chat;

    public MyMessage(){}

    public MyMessage(List<String> msg) {
        this.msg = msg;
    }

    public MyMessage(MessageType messageType, String from, List<String> msg, String lobby) {
        this.messageType = messageType;
        this.from = from;
        this.msg = msg;
        this.lobby = lobby;
    }
    public MyMessage(MessageType messageType, String from, List<String> msg, String lobby, GameData gameData, ChatRecord chatRecord) {
        this.messageType = messageType;
        this.from = from;
        this.msg = msg;
        this.lobby = lobby;
        this.gameData = gameData;
        this.chatRecord = chatRecord;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public List<String> getMsg() {
        return msg;
    }

    public void setMsg(List<String> msg) {
        this.msg = msg;
    }

    public String getLobby() {
        return lobby;
    }

    public void setLobby(String lobby) {
        this.lobby = lobby;
    }

    public GameData getGameData() {
        return gameData;
    }

    public void setGameData(GameData gameData) {
        this.gameData = gameData;
    }

    public ChatRecord getChatRecord() {
        return chatRecord;
    }

    public void setChatRecord(ChatRecord chatRecord) {
        this.chatRecord = chatRecord;
    }

    public GameDataCheck getRecoverType() {
        return recoverType;
    }

    public void setRecoverType(GameDataCheck recoverType) {
        this.recoverType = recoverType;
    }

    public List<ChatRecord> getChat() {
        return chat;
    }

    public void setChat(List<ChatRecord> chat) {
        this.chat = chat;
    }
}
