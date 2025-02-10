package umk.jakuburb.mars.Teraformacja.Marsa.message.gameData;

import com.fasterxml.jackson.annotation.JsonProperty;
import umk.jakuburb.mars.Teraformacja.Marsa.message.Area;
import umk.jakuburb.mars.Teraformacja.Marsa.message.ChatRecord;
import umk.jakuburb.mars.Teraformacja.Marsa.message.Resources;
import umk.jakuburb.mars.Teraformacja.Marsa.message.WinningPoints;
import umk.jakuburb.mars.Teraformacja.Marsa.rabbit.GameCoreQueue;

import java.io.Serializable;
import java.util.*;

public class GameData implements Serializable {
    //Wszystko to co widzi user jak gra

    @JsonProperty("chatMessage")
    private List<ChatRecord> chat;

    @JsonProperty("players")
    private List<String> players;

    @JsonProperty("mainCard")
    private HashMap<String, Long> mainCards;

    @JsonProperty("cards")
    private HashMap<String, List<Long>> cards;

    @JsonProperty("usedCardBlue")
    private HashMap<String, List<Long>> usedCardBlue;
    @JsonProperty("usedCardGreen")
    private HashMap<String, List<Long>> usedCardGreen;

    @JsonProperty("usedCardRed")
    private HashMap<String, List<Long>> usedCardRed;

    @JsonProperty("resources")
    private HashMap<String, Resources> resources;

    @JsonProperty("planet")
    private List<Area> planet;

    @JsonProperty("round")
    private int round;

    @JsonProperty("levels")
    private HashMap<String, Long> level;

    @JsonProperty("usersState")
    private HashMap<String, GameCoreQueue.UserState> usersState;

    @JsonProperty("winPoints")
    private WinningPoints winningPoints;

    @JsonProperty("title")
    private HashMap<String, String> titles;

    @JsonProperty("prize")
    private HashMap<String, Boolean> prize;

    public GameData(){
        chat = new ArrayList<>();
        players = new ArrayList<>();

        cards = new HashMap<>();
        mainCards = new HashMap<>();
        usedCardRed = new HashMap<>();
        usedCardBlue = new HashMap<>();
        usedCardGreen = new HashMap<>();
        resources = new HashMap<>();
        usersState = new HashMap<>();
        level = new HashMap<>();
        planet = new ArrayList<>();
        titles = new HashMap<>();
        prize = new HashMap<>();

        winningPoints = new WinningPoints();
        round = 1;
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

    public HashMap<String, Long> getMainCards() {
        return mainCards;
    }

    public void setMainCards(HashMap<String, Long> mainCards) {
        this.mainCards = mainCards;
    }

    public HashMap<String, List<Long>> getCards() {
        return cards;
    }

    public void setCards(HashMap<String, List<Long>> cards) {
        this.cards = cards;
    }

    public HashMap<String, List<Long>> getUsedCardBlue() {
        return usedCardBlue;
    }

    public void setUsedCardBlue(HashMap<String, List<Long>> usedCardBlue) {
        this.usedCardBlue = usedCardBlue;
    }

    public HashMap<String, List<Long>> getUsedCardGreen() {
        return usedCardGreen;
    }

    public void setUsedCardGreen(HashMap<String, List<Long>> usedCardGreen) {
        this.usedCardGreen = usedCardGreen;
    }

    public HashMap<String, List<Long>> getUsedCardRed() {
        return usedCardRed;
    }

    public void setUsedCardRed(HashMap<String, List<Long>> usedCardRed) {
        this.usedCardRed = usedCardRed;
    }

    public HashMap<String, Resources> getResources() {
        return resources;
    }

    public void setResources(HashMap<String, Resources> resources) {
        this.resources = resources;
    }

    public List<Area> getPlanet() {
        return planet;
    }

    public void setPlanet(List<Area> planet) {
        this.planet = planet;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public HashMap<String, Long> getLevel() {
        return level;
    }

    public void setLevel(HashMap<String, Long> level) {
        this.level = level;
    }

    public HashMap<String, GameCoreQueue.UserState> getUsersState() {
        return usersState;
    }

    public void setUsersState(HashMap<String, GameCoreQueue.UserState> usersState) {
        this.usersState = usersState;
    }

    public WinningPoints getWinningPoints() {
        return winningPoints;
    }

    public void setWinningPoints(WinningPoints winningPoints) {
        this.winningPoints = winningPoints;
    }

    public HashMap<String, String> getTitles() {
        return titles;
    }

    public void setTitles(HashMap<String, String> titles) {
        this.titles = titles;
    }

    public HashMap<String, Boolean> getPrize() {
        return prize;
    }

    public void setPrize(HashMap<String, Boolean> prize) {
        this.prize = prize;
    }
}
