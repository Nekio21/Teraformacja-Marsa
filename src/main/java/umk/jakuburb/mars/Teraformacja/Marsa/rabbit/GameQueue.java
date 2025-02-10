package umk.jakuburb.mars.Teraformacja.Marsa.rabbit;

import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import umk.jakuburb.mars.Teraformacja.Marsa.database.entity.Card;
import umk.jakuburb.mars.Teraformacja.Marsa.database.entity.CardSkills;
import umk.jakuburb.mars.Teraformacja.Marsa.database.repository.CardRepository;
import umk.jakuburb.mars.Teraformacja.Marsa.message.*;
import umk.jakuburb.mars.Teraformacja.Marsa.message.Error;
import umk.jakuburb.mars.Teraformacja.Marsa.message.gameData.GameData;
import umk.jakuburb.mars.Teraformacja.Marsa.message.gameData.GameDataProces;
import umk.jakuburb.mars.Teraformacja.Marsa.utils.Timerable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;

import static umk.jakuburb.mars.Teraformacja.Marsa.message.Area.*;
import static umk.jakuburb.mars.Teraformacja.Marsa.message.MessageType.*;
import static umk.jakuburb.mars.Teraformacja.Marsa.message.gameData.GameDataProces.check;
import static umk.jakuburb.mars.Teraformacja.Marsa.message.gameData.GameDataProces.useCard;

public class GameQueue extends GameCoreQueue implements Timerable {

    private CardRepository cardRepository;
    private HashMap<MessageType, Function<MyMessage, Error>> checkAndSave;
    private GameData gameDate;

    private HashMap<String, Boolean> activePlayers;
    private HashMap<String, Long> mainCardToChose;
    private HashMap<String, List<Long>> cardsToChose;
    private List<Long> drawCards;

    public static final String NAME = "GameQueue";
    private String playersExchange;
    private final int RoundTimeSetting = 60;
    private int price = 3;
    private int roundTime = 60;
    private int whoPlay = 0;
    private GameState gameState = GameState.WAITING;


    //TODO: o bakup nie sie ma co sie martwic bo przeciez dziala fifo
    //wiec jak beda dwa taski: backup, nowa wiadomosc
    //to jak najpierw bedzie backup to zrobi backup i potem doda nowa wiadomosc,
    //jesli na odwrot to najpierw da nowa wiadomosc, a potem backup, który bedzie juz z ta nowa wiadomoscia

    public GameQueue(String uniqName,String playersExchange, RabbitAdmin rabbitAdmin, RabbitTemplate rabbitTemplate) {
        super(uniqName, rabbitAdmin, rabbitTemplate);

        gameDate = new GameData();
        this.playersExchange = playersExchange;
        drawCards = new ArrayList<>();
        checkAndSave = new HashMap<>();
        activePlayers = new HashMap<>();
        mainCardToChose = new HashMap<>();
        cardsToChose = new HashMap<>();

        addReceiveFunction(MessageType.RECOVER, this::recover);


        addReceiveFunction(MessageType.MESSAGE_SEND, this::monitorUserMove);
        checkAndSave.put(MessageType.MESSAGE_SEND, this::messageSendCAS);

        addReceiveFunction(MessageType.PLAYER_IN_GAME, this::playerInGame);
        //checkAndSave.put(MessageType.PLAYER_IN_GAME, this::playerInGameCAS);

        addReceiveFunction(MessageType.MAIN_CARDS, this::mainCard);
        addReceiveFunction(CARDS10, this::card10Receive);

        addReceiveFunction(MessageType.USE_CARD, this::monitorUserMove);
        checkAndSave.put(MessageType.USE_CARD, this::useCardCAS);

        addReceiveFunction(MessageType.NEXT_ROUND, this::monitorUserMove);
        checkAndSave.put(MessageType.NEXT_ROUND, this::nextRoundCAS);

        addReceiveFunction(TEMP_UP, this::monitorUserMove);
        checkAndSave.put(TEMP_UP, this::tempUPCAS);

        addReceiveFunction(TITLE, this::monitorUserMove);
        checkAndSave.put(TITLE, this::titleCAS);

        addReceiveFunction(PRIZE, this::monitorUserMove);
        checkAndSave.put(PRIZE, this::prizeCAS);

        addReceiveFunction(PS, this::monitorUserMove);
        checkAndSave.put(PS, this::psCAS);

        addReceiveFunction(BOARD_TREE, this::boardTree);
        addReceiveFunction(BOARD_OCEAN, this::boardOcean);
        addReceiveFunction(BOARD_CITY, this::boardCity);

        addReceiveFunction(PUT_TREE, this::putTree);
        addReceiveFunction(PUT_CITY, this::putCity);
        addReceiveFunction(PUT_OCEAN, this::putOcean);
    }

    public void initGameData(List<String> players){
        gameDate.setPlayers(players);

        for(String player: gameDate.getPlayers()){
            gameDate.getResources().put(player, new Resources());
            gameDate.getCards().put(player, new ArrayList<>());
            gameDate.getMainCards().put(player, -99L);
            gameDate.getUsedCardBlue().put(player, new ArrayList<>());
            gameDate.getUsedCardRed().put(player, new ArrayList<>());
            gameDate.getUsedCardGreen().put(player, new ArrayList<>());
            gameDate.getLevel().put(player, 23L);

            gameDate.getUsersState().put(player, UserState.WAITING);
        }

        gameDate.setPlanet(List.of(
                NO_OCEAN, NOTHING, NOTHING,NO_OCEAN,NO_OCEAN,
                NOTHING, NOTHING, NOTHING,NOTHING,NOTHING, NO_OCEAN,
                NOTHING, NOTHING, NOTHING,NOTHING,NOTHING, NOTHING, NOTHING,
                NOTHING, NOTHING, NOTHING,NOTHING,NOTHING, NOTHING, NOTHING,NO_OCEAN,
                NOTHING, NOTHING, NOTHING,NO_OCEAN,NO_OCEAN, NO_OCEAN, NOTHING,NOTHING,NOTHING,
                NOTHING, NOTHING, NOTHING,NOTHING,NOTHING, NO_OCEAN, NO_OCEAN,NO_OCEAN,
                NOTHING, NOTHING, NOTHING,NOTHING,NOTHING, NOTHING, NOTHING,
                NOTHING, NOTHING, NOTHING,NOTHING,NOTHING, NOTHING,
                NOTHING, NOTHING, NOTHING,NO_OCEAN,NOTHING
        ));

        cardsToChose.clear();
    }


    private void card10Receive(MyMessage msg){
        if(gameDate.getUsersState().get(msg.getFrom()) != UserState.CHOSE_CARD) return;

        List<Long> ids = msg.getMsg().stream().map(Long::valueOf).toList();

        int gold = gameDate.getResources().get(msg.getFrom()).getGold();
        String from = msg.getFrom();

        if(ids.size()*price > gold){
            sendErrorMessage(from, Error.MONEY);
        }

        if(cardsToChose.get(from).containsAll(ids)){
            List<Card> list = cardRepository.getCards(ids);
            Error err = GameDataProces.buyCarts(from, list,price, gameDate);

            if(err != Error.NO_ERROR){
                sendErrorMessage(from, err);
                return;
            }

            sendCards(from, ids);
            sendResources(from);

            gameDate.getUsersState().put(from, UserState.WAITING);

            if(gameDate.getUsersState().values().stream().allMatch(e->e==UserState.WAITING)){
                startRound();
            }else{
                sendStates();
            }
        }else{
            sendErrorMessage(from, Error.DEFAULT);
        }
    }

    private void startRound(){
        gameState = GameState.ROUND;
        sendGameState();

        for(String p: gameDate.getPlayers()){
            gameDate.getUsersState().put(p, UserState.NO_MOVE);
            recover(new MyMessage(List.of(p)));
        }

        whoPlay = (whoPlay+1) % gameDate.getPlayers().size();
        gameDate.getUsersState().put(gameDate.getPlayers().get(whoPlay), UserState.FIRST_MOVE);
        sendStates();
    }

    private void endRound(){
        gameState= GameState.AFTER_ROUND;
        gameDate.getPlayers().forEach(e->{
            gameDate.getResources().get(e).fill(Math.toIntExact(gameDate.getLevel().get(e)));
            sendResources(e);
        });

        gameState= GameState.BEFORE_ROUND;
        gameDate.setRound(gameDate.getRound()+1);
        cardsToChose.clear();
        gameDate.getPlayers().forEach(e->{
            takeCard(e, false, 2);
        });
    }

    private void endGame(){
        System.out.println("endGame");
    }


    private Error psCAS(MyMessage msg){
        String about = msg.getAbout();

        if(gameDate.getUsersState().get(msg.getAbout()) != UserState.FIRST_MOVE && gameDate.getUsersState().get(msg.getAbout()) != UserState.SECOND_MOVE){
            return Error.NO_YOUR_MOVE;
        }

        WinningPoints winningPoints = gameDate.getWinningPoints();
        Resources resources = gameDate.getResources().get(about);
        AtomicInteger pz = new AtomicInteger();
        pz.set(0);

        Error error1;
        Error error2;

        if(msg.getMsg().get(0).equals("HEAT")){
            error1 = winningPoints.put(CardSkills.Resource.TEMP,1, true, pz);
            error2 = resources.put(CardSkills.Resource.GOLD, 14, false);
        }else if(msg.getMsg().get(0).equals("ENERGY")){
            error1 = resources.put(CardSkills.Resource.ENERGY_PROD, 11, true);
            error2 = resources.put(CardSkills.Resource.GOLD, 14, false);
        }else{
            return Error.DEFAULT;
        }

        if(error1 != Error.NO_ERROR){
            return error1;
        }
        else if(error2 != Error.NO_ERROR){
            return error2;
        }

        gameDate.setWinningPoints(winningPoints);
        gameDate.getResources().put(about, resources);
        gameDate.getLevel().put(about, gameDate.getLevel().get(about) + pz.get());


        if(gameDate.getUsersState().get(about) == UserState.FIRST_MOVE){
            gameDate.getUsersState().put(about, UserState.SECOND_MOVE);
        }
        else if(gameDate.getUsersState().get(msg.getAbout()) == UserState.SECOND_MOVE){
            nextRoundCAS(msg);
        }

        sendStates();
        sendResources(about);
        sendOthers(about);
        sendLevel(about);

        return Error.NO_ERROR;
    }

    private Error prizeCAS(MyMessage msg){
        String about = msg.getAbout();

        if(gameDate.getUsersState().get(msg.getAbout()) != UserState.FIRST_MOVE && gameDate.getUsersState().get(msg.getAbout()) != UserState.SECOND_MOVE){
            return Error.NO_YOUR_MOVE;
        }

        if(gameDate.getPrize().values().stream().count() > 2){
            return Error.NO_MORE_PRIZE;
        }

        int price = (int)gameDate.getPrize().values().stream().count() * 8;

        Resources resources = gameDate.getResources().get(about);
        Error error = resources.put(CardSkills.Resource.GOLD, price, false);

        if(error != Error.NO_ERROR){
            return error;
        }

        ArrayList tab = new ArrayList(List.of(
                "PZ", "GOLD", "LEAF", "ENERGY", "HEAT"
        ));

        if(gameDate.getPrize().get(msg.getMsg().get(0)) != null){
            return Error.OCCUPIED;
        }

        if(!tab.contains(msg.getMsg().get(0))){
            return Error.DEFAULT;
        }

        gameDate.getPrize().put(msg.getMsg().get(0), true);

        if(gameDate.getUsersState().get(about) == UserState.FIRST_MOVE){
            gameDate.getUsersState().put(about, UserState.SECOND_MOVE);
        }
        else if(gameDate.getUsersState().get(msg.getAbout()) == UserState.SECOND_MOVE){
            nextRoundCAS(msg);
        }

        msg.setOwners(gameDate.getPlayers());

        gameDate.getResources().put(about, resources);

        sendStates();
        sendResources(about);

        return Error.NO_ERROR;
    }

    private Error titleCAS(MyMessage msg){
        String about = msg.getAbout();

        if(gameDate.getUsersState().get(msg.getAbout()) != UserState.FIRST_MOVE && gameDate.getUsersState().get(msg.getAbout()) != UserState.SECOND_MOVE){
            return Error.NO_YOUR_MOVE;
        }

        if(gameDate.getTitles().values().stream().count() > 2){
            return Error.NO_MORE_TITLE;
        }

        Resources resources = gameDate.getResources().get(about);
        Error error = resources.put(CardSkills.Resource.GOLD, 8, false);

        if(error != Error.NO_ERROR){
            return error;
        }

        if(msg.getMsg().get(0).equals("PZ")){
            if((gameDate.getLevel().get(about) >= 35)&&gameDate.getTitles().get("PZ") == null){
                gameDate.getTitles().put("PZ", about);
            }else {
                return Error.TITLE;
            }
        }
        else if(msg.getMsg().get(0).equals("CITY")){

            if(gameDate.getTitles().get("CITY") != null){
                return Error.OCCUPIED;
            }

            Area a;
            int index = gameDate.getPlayers().indexOf(about);

            if(index == 0){
                a = CITY_P1;
            }else if(index == 1){
                a = CITY_P2;
            }else if(index == 2){
                a = CITY_P3;
            }else {
                return Error.DEFAULT;
            }

            int count = 0;

            for(Area area: gameDate.getPlanet()){
                if(area == a){
                    count++;
                }
            }

            if(count >= 3){
                gameDate.getTitles().put("CITY", about);
            }else{
                return Error.TITLE;
            }
        }
        else if(msg.getMsg().get(0).equals("FOREST")){
            if(gameDate.getTitles().get("FOREST") != null){
                return Error.OCCUPIED;
            }

            Area a;
            int index = gameDate.getPlayers().indexOf(about);

            if(index == 0){
                a = TREE_P1;
            }else if(index == 1){
                a = TREE_P2;
            }else if(index == 2){
                a = TREE_P3;
            }else {
                return Error.DEFAULT;
            }

            int count = 0;

            for(Area area: gameDate.getPlanet()){
                if(area == a){
                    count++;
                }
            }

            if(count >= 3){
                gameDate.getTitles().put("FOREST", about);
            }else{
                return Error.TITLE;
            }
        }
        else if(msg.getMsg().get(0).equals("CARD")){
            if((gameDate.getCards().get(about).stream().count() >= 15)&&gameDate.getTitles().get("CARD") == null){
                gameDate.getTitles().put("CARD", about);
            }else{
                return Error.TITLE;
            }
        }
        else if(msg.getMsg().get(0).equals("SYMBOLS")){

        }
        else{
            return Error.DEFAULT;
        }

        if(gameDate.getUsersState().get(about) == UserState.FIRST_MOVE){
            gameDate.getUsersState().put(about, UserState.SECOND_MOVE);
        }
        else if(gameDate.getUsersState().get(msg.getAbout()) == UserState.SECOND_MOVE){
            nextRoundCAS(msg);
        }

        msg.setOwners(gameDate.getPlayers());

        gameDate.getResources().put(about, resources);

        sendStates();
        sendResources(about);

        return Error.NO_ERROR;
    }

    private Error nextRoundCAS(MyMessage msg){
        if(gameDate.getUsersState().get(msg.getAbout()) != UserState.FIRST_MOVE && gameDate.getUsersState().get(msg.getAbout()) != UserState.SECOND_MOVE){
            return Error.NO_YOUR_MOVE;
        }

        if(gameDate.getUsersState().get(msg.getAbout()) == UserState.FIRST_MOVE){
            gameDate.getUsersState().put(msg.getAbout(), UserState.PASS);
        }

        if(gameDate.getUsersState().get(msg.getAbout()) == UserState.SECOND_MOVE){
            gameDate.getUsersState().put(msg.getAbout(), UserState.NO_MOVE);
        }

        nextPlayer();
        sendStates();

        return Error.NO_ERROR;
    }

    private void boardCity(MyMessage msg){
        String about = msg.getAbout();
        Area area;

        switch (gameDate.getPlayers().indexOf(about)){
            case 0 -> {
                area = CITY_P1;
            }
            case 1 -> {
                area = CITY_P2;
            }
            case 2 -> {
                area = CITY_P3;
            }
            default -> {
                sendErrorMessage(about, Error.DEFAULT);
                return;
            }
        }

        board(area, msg);
    }

    private void boardTree(MyMessage msg){
        String about = msg.getAbout();
        Area area;

        switch (gameDate.getPlayers().indexOf(about)){
            case 0 -> {
                area = TREE_P1;
            }
            case 1 -> {
                area = TREE_P2;
            }
            case 2 -> {
                area = TREE_P3;
            }
            default -> {
                sendErrorMessage(about, Error.DEFAULT);
                return;
            }
        }

        board(area, msg);
    }

    private void boardOcean(MyMessage msg){
        board(OCEAN, msg);
    }

    private void board(Area a, MyMessage msg){
        String about = msg.getAbout();

        if(gameDate.getUsersState().get(msg.getAbout()) != UserState.FIRST_MOVE && gameDate.getUsersState().get(msg.getAbout()) != UserState.SECOND_MOVE){
            sendErrorMessage(about, Error.NO_YOUR_MOVE);
            return;
        }

        Area area = a;

        List<Area> list;

        try {
            list = Board.getBoardTF(area, gameDate.getPlanet());
        } catch (Exception e) {
            sendErrorMessage(about, Error.DEFAULT);
            return;
        }

        msg.setMsg(list.stream().map(Enum::toString).toList());

        sendToUser(msg);
    }

    private void putOcean(MyMessage msg){
        String about = msg.getAbout();

        if(gameDate.getUsersState().get(msg.getAbout()) != UserState.FIRST_MOVE && gameDate.getUsersState().get(msg.getAbout()) != UserState.SECOND_MOVE){
            sendErrorMessage(about, Error.NO_YOUR_MOVE);
            return;
        }

        Area ocean = OCEAN;

        int areaIndex = Integer.parseInt(msg.getMsg().get(0));
        List<Area> list = gameDate.getPlanet();

        if(list.get(areaIndex) != NOTHING && list.get(areaIndex) != NO_OCEAN){
            sendErrorMessage(about, Error.AREA_OCCUPIED);
        }

        ArrayList<Area> listNew = new ArrayList<>();
        for(Area a: list){
            listNew.add(a);
        }

        WinningPoints winningPoints = gameDate.getWinningPoints();
        Resources resources = gameDate.getResources().get(about);
        AtomicInteger pz = new AtomicInteger();
        List<Area> freeAreas;

        pz.set(0);

        try {
            freeAreas = Board.getBoardTF(ocean, listNew);
        } catch (Exception e) {
            sendErrorMessage(about, Error.DEFAULT);
            return;
        }

        if(freeAreas.get(areaIndex) == TRUE){
            listNew.set(areaIndex, ocean);
            Error error1 = winningPoints.put(CardSkills.Resource.OCEAN,1, true, pz);
            Error error2;
            error2 = resources.put(CardSkills.Resource.GOLD, 18, false);


            if(error1 != Error.NO_ERROR){
                sendErrorMessage(about, Error.OXYGEN);
                return;
            }
            if(error2 != Error.NO_ERROR){
                sendErrorMessage(about, Error.PLANTS);
                return;
            }
        }
        else{
            sendErrorMessage(about, Error.DEFAULT);
            return;
        }

        gameDate.setWinningPoints(winningPoints);
        gameDate.getResources().put(about, resources);
        gameDate.getLevel().put(about, gameDate.getLevel().get(about) + pz.get());
        gameDate.setPlanet(listNew);

        if(gameDate.getUsersState().get(about) == UserState.FIRST_MOVE){
            gameDate.getUsersState().put(about, UserState.SECOND_MOVE);
        }
        else if(gameDate.getUsersState().get(msg.getAbout()) == UserState.SECOND_MOVE){
            nextRoundCAS(msg);
        }

        sendStates();
        sendResources(about);
        sendOthers(about);
        sendLevel(about);
        sendPlanet(about);
    }

    private void putCity(MyMessage msg){
        String about = msg.getAbout();

        if(gameDate.getUsersState().get(msg.getAbout()) != UserState.FIRST_MOVE && gameDate.getUsersState().get(msg.getAbout()) != UserState.SECOND_MOVE){
            sendErrorMessage(about, Error.NO_YOUR_MOVE);
            return;
        }

        Area city;

        switch (gameDate.getPlayers().indexOf(msg.getAbout())){
            case 0 -> city = CITY_P1;
            case 1 -> city = CITY_P2;
            case 2 -> city = CITY_P3;
            default -> {
                sendErrorMessage(about, Error.DEFAULT);
                return;
            }
        }

        int areaIndex = Integer.parseInt(msg.getMsg().get(0));
        List<Area> list = gameDate.getPlanet();

        if(list.get(areaIndex) != NOTHING && list.get(areaIndex) != NO_OCEAN){
            sendErrorMessage(about, Error.AREA_OCCUPIED);
        }

        ArrayList<Area> listNew = new ArrayList<>();
        for(Area a: list){
            listNew.add(a);
        }

        Resources resources = gameDate.getResources().get(about);
        List<Area> freeAreas;

        try {
            freeAreas = Board.getBoardTF(city, listNew);
        } catch (Exception e) {
            sendErrorMessage(about, Error.DEFAULT);
            return;
        }

        if(freeAreas.get(areaIndex) == TRUE){
            listNew.set(areaIndex, city);
            Error error2;
            error2 = resources.put(CardSkills.Resource.GOLD, 25, false);
            Error error1 = resources.put(CardSkills.Resource.GOLD_PROD, 1, true);

            if(error1 != Error.NO_ERROR){
                sendErrorMessage(about, error1);
                return;
            }
            if(error2 != Error.NO_ERROR){
                sendErrorMessage(about, Error.CITY);
                return;
            }
        }
        else{
            sendErrorMessage(about, Error.DEFAULT);
            return;
        }

        gameDate.getResources().put(about, resources);
        gameDate.setPlanet(listNew);

        if(gameDate.getUsersState().get(about) == UserState.FIRST_MOVE){
            gameDate.getUsersState().put(about, UserState.SECOND_MOVE);
        }
        else if(gameDate.getUsersState().get(msg.getAbout()) == UserState.SECOND_MOVE){
            nextRoundCAS(msg);
        }

        sendStates();
        sendResources(about);
        sendOthers(about);
        sendPlanet(about);
    }

    private void putTree(MyMessage msg){
        String about = msg.getAbout();

        if(gameDate.getUsersState().get(msg.getAbout()) != UserState.FIRST_MOVE && gameDate.getUsersState().get(msg.getAbout()) != UserState.SECOND_MOVE){
            sendErrorMessage(about, Error.NO_YOUR_MOVE);
            return;
        }

        Area tree;

        switch (gameDate.getPlayers().indexOf(msg.getAbout())){
            case 0 -> tree = TREE_P1;
            case 1 -> tree = TREE_P2;
            case 2 -> tree = TREE_P3;
            default -> {
                sendErrorMessage(about, Error.DEFAULT);
                return;
            }
        }

        int areaIndex = Integer.parseInt(msg.getMsg().get(0));
        List<Area> list = gameDate.getPlanet();

        if(list.get(areaIndex) != NOTHING && list.get(areaIndex) != NO_OCEAN){
            sendErrorMessage(about, Error.AREA_OCCUPIED);
        }

        ArrayList<Area> listNew = new ArrayList<>();
        for(Area a: list){
            listNew.add(a);
        }

        WinningPoints winningPoints = gameDate.getWinningPoints();
        Resources resources = gameDate.getResources().get(about);
        AtomicInteger pz = new AtomicInteger();
        List<Area> freeAreas;

        pz.set(0);

        try {
            freeAreas = Board.getBoardTF(tree, listNew);
        } catch (Exception e) {
            sendErrorMessage(about, Error.DEFAULT);
            return;
        }



        if(freeAreas.get(areaIndex) == TRUE){
            listNew.set(areaIndex, tree);
            Error error1 = winningPoints.put(CardSkills.Resource.OXYGEN,1, true, pz);
            Error error2;
            if(msg.getMsg().get(1).equals("money")){
                error2 = resources.put(CardSkills.Resource.GOLD, 23, false);
            }else if(msg.getMsg().get(1).equals("leaf")){
                error2 = resources.put(CardSkills.Resource.PLANTS, resources.getPlantsToForest(), false);
            }else{
                sendErrorMessage(about, Error.DEFAULT);
                return;
            }

            if(error1 != Error.NO_ERROR){
                sendErrorMessage(about, Error.OXYGEN);
                return;
            }
            if(error2 != Error.NO_ERROR){
                sendErrorMessage(about, Error.PLANTS);
                return;
            }
        }
        else{
            sendErrorMessage(about, Error.DEFAULT);
            return;
        }

        gameDate.setWinningPoints(winningPoints);
        gameDate.getResources().put(about, resources);
        gameDate.getLevel().put(about, gameDate.getLevel().get(about) + pz.get());
        gameDate.setPlanet(listNew);

        if(gameDate.getUsersState().get(about) == UserState.FIRST_MOVE){
            gameDate.getUsersState().put(about, UserState.SECOND_MOVE);
        }
        else if(gameDate.getUsersState().get(msg.getAbout()) == UserState.SECOND_MOVE){
            nextRoundCAS(msg);
        }

        sendStates();
        sendResources(about);
        sendOthers(about);
        sendLevel(about);
        sendPlanet(about);
    }

    private Error tempUPCAS(MyMessage msg){
        String about = msg.getAbout();

        if(gameDate.getUsersState().get(msg.getAbout()) != UserState.FIRST_MOVE && gameDate.getUsersState().get(msg.getAbout()) != UserState.SECOND_MOVE){
            return Error.NO_YOUR_MOVE;
        }

        Resources resources = gameDate.getResources().get(about);
        WinningPoints winningPoints = gameDate.getWinningPoints();

        if(resources.getHeatToTemp() >  resources.getHeat()){
            return Error.HEAT;
        }

        AtomicInteger pz = new AtomicInteger();
        pz.set(0);

        Error error = resources.put(CardSkills.Resource.HEAT, resources.getHeatToTemp(), false);
        Error error2 = winningPoints.put(CardSkills.Resource.TEMP, 1, true , pz);

        if(error != Error.NO_ERROR){
            return error;
        }else if(error2 != Error.NO_ERROR){
            return error2;
        }

        gameDate.getResources().put(about, resources);
        gameDate.setWinningPoints(winningPoints);
        gameDate.getLevel().put(about, gameDate.getLevel().get(about) + pz.get());

        if(gameDate.getUsersState().get(about) == UserState.FIRST_MOVE){
            gameDate.getUsersState().put(about, UserState.SECOND_MOVE);
        }
        else if(gameDate.getUsersState().get(msg.getAbout()) == UserState.SECOND_MOVE){
            nextRoundCAS(msg);
        }

        sendStates();
        sendResources(about);
        sendOthers(about);
        sendLevel(about);

        return Error.NO_ERROR;
    }


    public Error useCardCAS(MyMessage msg){
        String about = msg.getAbout();

        if(gameDate.getUsersState().get(msg.getAbout()) != UserState.FIRST_MOVE && gameDate.getUsersState().get(msg.getAbout()) != UserState.SECOND_MOVE){
            return Error.NO_YOUR_MOVE;
        }

        msg.setAbout(msg.getFrom());
        List<Card> list = cardRepository.getCards(List.of(Long.valueOf(msg.getMsg().get(0))));

        Error error = useCard(about, list.get(0), gameDate);

        if(error != Error.NO_ERROR){
            return error;
        }

        gameDate.getCards().get(msg.getAbout()).removeIf(e->e.equals(Long.valueOf(msg.getMsg().get(0))));

        if(gameDate.getUsersState().get(about) == UserState.FIRST_MOVE){
            gameDate.getUsersState().put(about, UserState.SECOND_MOVE);
        }
        else if(gameDate.getUsersState().get(msg.getAbout()) == UserState.SECOND_MOVE){
            nextRoundCAS(msg);
        }

        msg.setCards(List.of(new CardToSend(list.get(0))));

        sendStates();
        sendResources(about);
        sendOthers(about);
        sendLevel(about);

        return Error.NO_ERROR;
    }

    private void nextPlayer(){
        nextPlayer(0);
    }

    private void nextPlayer(int p){
        if(p > 4){
            endRound();
            return;
        }
        roundTime = RoundTimeSetting;
        whoPlay = (whoPlay+1)%gameDate.getPlayers().size();

        String whoPlayString = gameDate.getPlayers().get(whoPlay);

        if(gameDate.getUsersState().get(whoPlayString) == UserState.NO_MOVE){
            gameDate.getUsersState().put(gameDate.getPlayers().get(whoPlay), UserState.FIRST_MOVE);
            sendStates();
        }else{
            nextPlayer(p+1);
        }
    }

    public void mainCard(MyMessage message){
        if(gameDate.getUsersState().get(message.getAbout()) == UserState.CHOSE_MAIN_CARD && Long.valueOf(message.getMsg().get(0)).equals(mainCardToChose.get(message.getFrom()))){
            String from = message.getFrom();

            Card card = cardRepository.getCard(Long.valueOf(message.getMsg().get(0)));
            gameDate.getMainCards().put(from, Long.valueOf(message.getMsg().get(0)));

            GameDataProces.useCard(from, card, gameDate);

            gameDate.getUsersState().put(message.getAbout(), UserState.CHOSE_CARD);

            message.setMsg(List.of(from));
            message.setMessageType(MessageType.MAIN_CARDS_SAVE);
            message.setAbout(from);

            sendToUsers(message);
            sendResources(from);
            sendOthers(from);
            sendStates();
            sendCards10(message);
        } else{
            sendErrorMessage(message.getFrom(), Error.DEFAULT);
        }
    }

    private void sendStates(){
        MyMessage myMessage = new MyMessage();
        myMessage.setFrom(NAME);
        myMessage.setMessageType(USER_STATE);
        myMessage.setMsg(gameDate.getPlayers().stream().map(e->gameDate.getUsersState().get(e).toString()).toList());
        myMessage.setOwners(gameDate.getPlayers());

        sendToUsers(myMessage);
    }

    private void sendGameState(){
        MyMessage myMessage = new MyMessage();
        myMessage.setFrom(NAME);
        myMessage.setMessageType(GAME_STATE);
        myMessage.setMsg(List.of(gameState.toString()));

        sendToUsers(myMessage);
    }

    private void sendResources(String user){
        MyMessage myMessage = new MyMessage();
        myMessage.setFrom(NAME);
        myMessage.setAbout(user);
        myMessage.setMessageType(MessageType.RESOURCES);
        myMessage.setResources(gameDate.getPlayers().stream().map(e->gameDate.getResources().get(e)).toList());
        myMessage.setOwners(gameDate.getPlayers());

        sendToUsers(myMessage);
    }

    private void sendPlanet(String user){
        MyMessage myMessage = new MyMessage();
        myMessage.setFrom(NAME);
        myMessage.setAbout(user);
        myMessage.setMessageType(MessageType.PLANET);
        myMessage.setMsg(gameDate.getPlanet().stream().map(Enum::toString).toList());

        sendToUsers(myMessage);
    }

    private void sendLevel(String user){
        MyMessage myMessage = new MyMessage();
        myMessage.setFrom(NAME);
        myMessage.setAbout(user);
        myMessage.setMessageType(MessageType.LEVELS);
        myMessage.setOwners(gameDate.getPlayers());
        myMessage.setDataLong(gameDate.getPlayers().stream().map(e->gameDate.getLevel().get(e)).toList());

        sendToUsers(myMessage);
    }

    private void sendOthers(String user){
        MyMessage myMessage = new MyMessage();
        myMessage.setFrom(NAME);
        myMessage.setAbout(user);
        myMessage.setMessageType(MessageType.OTHERS);
        myMessage.setMsg(List.of(
                gameDate.getRound(),
                gameDate.getWinningPoints().getWinTemp(), gameDate.getWinningPoints().getTemp(),
                gameDate.getWinningPoints().getWinOxygen(), gameDate.getWinningPoints().getOxygen(),
                gameDate.getWinningPoints().getWinOcean(), gameDate.getWinningPoints().getOcean()
        ).stream().map(String::valueOf).toList());

        sendToUsers(myMessage);
    }

    private void sendCards(String user, List<Long> cardToSends){
        List<Card> cards = cardRepository.getCards(cardToSends);

        MyMessage myMessage = new MyMessage();
        myMessage.setFrom(NAME);
        myMessage.setAbout(user);
        myMessage.setMessageType(MessageType.CARDS);
        myMessage.setCards(cards.stream().map(e->new CardToSend(e.getId(), e.getTypeCard(), e.getImage())).toList());
        myMessage.setOwners(gameDate.getPlayers());

        sendToUser(myMessage);
    }

    private void takeCard(String user, boolean cards10, int amount){
        gameDate.getUsersState().put(user, UserState.CHOSE_CARD);
        sendStates();

        List<Card> cardsList = cardRepository.getRandomCards(drawCards, amount);

        if(cardsList.size() != amount){
            sendErrorMessage(user, Error.NO_MORE_CARD);
            return;
        }

        drawCards.addAll(cardsList.stream().map(Card::getId).toList());

        cardsToChose.put(user,cardsList.stream().map(Card::getId).toList());

        List<CardToSend> cardToSends = cardsList.stream().map(c->new CardToSend(c.getId(), c.getTypeCard(), c.getImage())).toList();

        MyMessage myMessage = new MyMessage();
        myMessage.setCards(cardToSends);
        myMessage.setMessageType(MessageType.CARDS10);
        myMessage.setFrom(user);
        myMessage.setAbout(user);

        sendToUser(myMessage);
    }

    private void sendCards10(MyMessage message){
        takeCard(message.getAbout(), true, 3);
    }

    public void recover(MyMessage message){
        String from = message.getMsg().get(0);
        String addressQueue = CoreQueue.PRESUB_BEFORE_NAME + from;

        MyMessage msg = new MyMessage();
        msg.setFrom(NAME);
        msg.setMessageType(MessageType.RECOVER);
        msg.setGameData(gameDate);

        rabbitTemplate.convertAndSend(addressQueue, msg);
        System.out.println("helped :)");
    }

    public Error messageSendCAS(MyMessage message){
        ChatRecord chatRecord = new ChatRecord(message.getFrom(), message.getMsg().get(0));
        gameDate.getChat().add(chatRecord);

        return Error.NO_ERROR;
    }

    private void playerInGame(MyMessage msg){
        activePlayers.put(msg.getFrom(), true);

        if(gameState != GameState.WAITING){
            return;
        }

        boolean all = gameDate.getPlayers().stream().allMatch(e->activePlayers.getOrDefault(e, false));

        gameState = all ? GameState.CHOSE_CARDS : GameState.WAITING;

        MyMessage msg2 = new MyMessage();
        msg2.setFrom(NAME);
        msg2.setMessageType(GAME_STATE);
        msg2.setMsg(List.of(gameState.toString()));
        rabbitTemplate.convertAndSend(playersExchange, "", msg2);

        sendStates();

        if(gameState == GameState.CHOSE_CARDS) {
            giveMainCards();
        }

    }

    private void giveMainCards(){
        List<Card> mainCards =  cardRepository.getRandomMainCard();
        String addressQueue;

        gameState = GameState.CHOSE_CARDS;
        sendGameState();

        for(int i=0;i<gameDate.getPlayers().size();i++){
            gameDate.getUsersState().put(gameDate.getPlayers().get(i), UserState.CHOSE_MAIN_CARD);
            addressQueue = CoreQueue.PRESUB_BEFORE_NAME + gameDate.getPlayers().get(i);

            CardToSend card = new CardToSend();
            card.setTypeCard(mainCards.get(i).getTypeCard());
            card.setImage(mainCards.get(i).getImage());
            card.setIndex(mainCards.get(i).getId());

            mainCardToChose.put(gameDate.getPlayers().get(i), mainCards.get(i).getId());

            MyMessage msg2 = new MyMessage();
            msg2.setFrom(NAME);
            msg2.setMessageType(MessageType.MAIN_CARDS);
            msg2.setCards(List.of(card));
            rabbitTemplate.convertAndSend(addressQueue, msg2);
        }
    }

    @Override
    protected MyMessage notFoundTypeReceive(MyMessage m) {

        return null;
    }

    @Override
    public void doThing() {
        if(gameState==GameState.ROUND) {
            MyMessage myMessage = new MyMessage();
            myMessage.setFrom(NAME);
            myMessage.setMessageType(MessageType.CLOCK);
            myMessage.setMsg(List.of(gameDate.getPlayers().get(whoPlay), String.valueOf(roundTime)));

            rabbitTemplate.convertAndSend(playersExchange, "", myMessage);

            roundTime--;

            if (roundTime < 0) {
                //whoPlay = (whoPlay + 1) % gameDate.getPlayers().size();
                MyMessage message = new MyMessage();
                message.setAbout(gameDate.getPlayers().get(whoPlay));
                nextRoundCAS(message);
            }
        }
    }

    public GameData getGameDate() {
        return gameDate;
    }

    public void setGameDate(GameData gameDate) {
        this.gameDate = gameDate;
    }

    public Error checkAndSaveDefault(MyMessage message){
        return Error.DEFAULT;
    }

    public boolean checkAndSaveDefaultTrue(MyMessage message){
        return true;
    }

    public void monitorUserMove(MyMessage message){
        //wysyła recover

        for(String uniqPlayersName: gameDate.getPlayers()){
            recover(new MyMessage(List.of(uniqPlayersName)));
        }

        //sprawdza czy moze
        //zapisuje rzeczy ...
        Error err = checkAndSave.getOrDefault(message.getMessageType(), this::checkAndSaveDefault).apply(message);

        //przekazuje informacje
        if(err == Error.NO_ERROR){
            rabbitTemplate.convertAndSend(playersExchange, "", message);
        }else{
            sendErrorMessage(message.getFrom(), err);
        }
    }

    private void sendErrorMessage(String from, Error error){
        MyMessage myMessage = new MyMessage();
        myMessage.setMessageType(MessageType.ERROR);
        myMessage.setFrom(from);
        myMessage.setAbout(from);
        myMessage.setError(error);

        sendToUser(myMessage);
    }

    private void sendToUser(MyMessage message){
        String from = message.getAbout();
        String addressQueue = CoreQueue.PRESUB_BEFORE_NAME + from;

        message.setFrom(NAME);

        rabbitTemplate.convertAndSend(addressQueue, message);
    }

    private void sendToUsers(MyMessage message){
        message.setFrom(NAME);
        rabbitTemplate.convertAndSend(playersExchange, "", message);
    }

    public void setCardRepository(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }
}
