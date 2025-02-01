package umk.jakuburb.mars.Teraformacja.Marsa.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import umk.jakuburb.mars.Teraformacja.Marsa.message.gameData.GameData;
import umk.jakuburb.mars.Teraformacja.Marsa.message.gameData.GameDataCheck;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyMessage implements Serializable {

    @JsonProperty("messageType")
    private MessageType messageType;

    @JsonProperty("recoverType")
    private GameDataCheck recoverType;

    @JsonProperty("from")
    private String from;

    @JsonProperty("about")
    private String about;

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

    @JsonProperty("cards")
    private List<CardToSend> cards;

    @JsonProperty("resources")
    private List<Resources> resources;

    @JsonProperty("dataLong")
    private List<Long> dataLong;

    @JsonProperty("dataListLong")
    private List<List<Long>> dataListLong;

    @JsonProperty("owners")
    private List<String> owners;

    @JsonProperty("error")
    private Error error;

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

    public MyMessage(MessageType messageType, String from, List<Resources> resources, List<String> msg) {
        this.messageType = messageType;
        this.from = from;
        this.resources = resources;
        this.msg = msg;
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

    public List<CardToSend> getCards() {
        return cards;
    }

    public void setCards(List<CardToSend> cards) {
        this.cards = cards;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public void setResources(List<Resources> resources) {
        this.resources = resources;
    }

    public void setDataLong(List<Long> dataLong) {
        this.dataLong = dataLong;
    }

    public void setDataListLong(List<List<Long>> dataListLong) {
        this.dataListLong = dataListLong;
    }

    public List<String> getOwners() {
        return owners;
    }

    public void setOwners(List<String> owners) {
        this.owners = owners;
    }

    public List<Resources> getResources() {
        return resources;
    }

    public List<Long> getDataLong() {
        return dataLong;
    }

    public List<List<Long>> getDataListLong() {
        return dataListLong;
    }
}
