package umk.jakuburb.mars.Teraformacja.Marsa.message.gameData;

import umk.jakuburb.mars.Teraformacja.Marsa.database.entity.Card;
import umk.jakuburb.mars.Teraformacja.Marsa.database.entity.CardSkills;
import umk.jakuburb.mars.Teraformacja.Marsa.message.Error;
import umk.jakuburb.mars.Teraformacja.Marsa.message.Resources;
import umk.jakuburb.mars.Teraformacja.Marsa.message.WinningPoints;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static umk.jakuburb.mars.Teraformacja.Marsa.database.entity.CardSkills.Resource.PZ;

public class GameDataProces {

    public static Error useCard(String user, Card card, GameData gameData){
        Resources resources = gameData.getResources().get(user);
        WinningPoints winningPoints = gameData.getWinningPoints();

        Error error = resources.put(CardSkills.Resource.GOLD, card.getPrice(), false);

        if(error == Error.MONEY){
            return error;
        }

        AtomicInteger pz = new AtomicInteger();
        pz.set(0);

        for(CardSkills cs: card.getCardSkillsList()){
            boolean plus = cs.getMove()!=CardSkills.Move.LOOSE;
            Error error1 = resources.put(cs.getResource(), cs.getAmount(), plus);
            Error error2 = winningPoints.put(cs.getResource(), cs.getAmount(), plus, pz);

            if(cs.getResource() == PZ){
                pz.set(pz.get() + 1);
            }

            if(error1 != Error.NO_ERROR && error2 != Error.NO_ERROR){
                return error;
            }
        }

        if(card.getTypeCard() == Card.TypeCard.MAIN){
            gameData.getMainCards().put(user, card.getId());
        }
        else if(card.getTypeCard() == Card.TypeCard.GREEN){
            List<Long> list = gameData.getUsedCardGreen().get(user);
            list.add(card.getId());
            gameData.getUsedCardGreen().put(user, list);
        }
        else if(card.getTypeCard() == Card.TypeCard.BLUE){
            List<Long> list = gameData.getUsedCardBlue().get(user);
            list.add(card.getId());
            gameData.getUsedCardBlue().put(user, list);
        }
        else if(card.getTypeCard() == Card.TypeCard.RED){
            List<Long> list = gameData.getUsedCardRed().get(user);
            list.add(card.getId());
            gameData.getUsedCardRed().put(user, list);
        }

        gameData.getResources().put(user, resources);
        gameData.setWinningPoints(winningPoints);
        gameData.getLevel().put(user, gameData.getLevel().get(user) + pz.get());

        return Error.NO_ERROR;
    }

    public static Error buyCarts(String user, List<Card> cards,int buyPrice, GameData gameData){
        int fullPrice = cards.size() * buyPrice;

        if(fullPrice > gameData.getResources().get(user).getGold()){
            return Error.MONEY;
        }

        gameData.getResources().get(user).put(CardSkills.Resource.GOLD, fullPrice, false);

        List<Long> newList = new ArrayList<>();

        for(Long ids: gameData.getCards().get(user)){
            newList.add(ids);
        }

        for(Long id: cards.stream().map(e->e.getId()).toList()){
            newList.add(id);
        }

        gameData.getCards().put(user, newList);

        return Error.NO_ERROR;
    }
    public static List<GameDataCheck> check(GameData gd1, GameData gd2, String username){
        List<GameDataCheck> list = new ArrayList<>();

        if(!Arrays.deepEquals(gd1.getChat().toArray(), gd2.getChat().toArray())){
            list.add(GameDataCheck.CHAT);
        }

        if(!Arrays.deepEquals(gd1.getPlayers().toArray(), gd2.getPlayers().toArray())){
            list.add(GameDataCheck.PLAYERS);
        }

        if(!(Arrays.deepEquals(
                gd1.getMainCards().keySet().toArray(new String[0]),
                gd2.getMainCards().keySet().toArray(new String[0]))  &&
                Arrays.deepEquals(
                        gd1.getMainCards().values().toArray(),
                        gd2.getMainCards().values().toArray())
        )){
            list.add(GameDataCheck.MAIN_CARD);
        }

        if(!(Arrays.deepEquals(
                gd1.getUsedCardBlue().keySet().toArray(new String[0]),
                gd2.getUsedCardBlue().keySet().toArray(new String[0]))  &&
                Arrays.deepEquals(
                        gd1.getUsedCardBlue().values().toArray(),
                        gd2.getUsedCardBlue().values().toArray())
        )){
            list.add(GameDataCheck.USED_CARDS_BLUE);
        }

        if(!(Arrays.deepEquals(
                gd1.getUsedCardGreen().keySet().toArray(new String[0]),
                gd2.getUsedCardGreen().keySet().toArray(new String[0]))  &&
                Arrays.deepEquals(
                        gd1.getUsedCardGreen().values().toArray(),
                        gd2.getUsedCardGreen().values().toArray())
        )){
            list.add(GameDataCheck.USED_CARDS_GREEN);
        }

        if(!(Arrays.deepEquals(
                gd1.getUsedCardRed().keySet().toArray(new String[0]),
                gd2.getUsedCardRed().keySet().toArray(new String[0]))  &&
                Arrays.deepEquals(
                        gd1.getUsedCardRed().values().toArray(),
                        gd2.getUsedCardRed().values().toArray())
        )){
            list.add(GameDataCheck.USED_CARDS_RED);
        }

        if(!Arrays.deepEquals(gd1.getCards().get(username).toArray(), gd2.getCards().get(username).toArray())){
            list.add(GameDataCheck.CARD);
        }

        if(!(Arrays.deepEquals(
                gd1.getResources().keySet().toArray(new String[0]),
                gd2.getResources().keySet().toArray(new String[0]))  &&
                Arrays.deepEquals(
                        gd1.getResources().values().toArray(),
                        gd2.getResources().values().toArray())
        )){
            list.add(GameDataCheck.RESOURCES);
        }

        if(!Arrays.deepEquals(gd1.getPlanet().toArray(), gd2.getPlanet().toArray())){
            list.add(GameDataCheck.PLANET);
        }

        if(!(Arrays.deepEquals(
                gd1.getLevel().keySet().toArray(new String[0]),
                gd2.getLevel().keySet().toArray(new String[0]))  &&
                Arrays.deepEquals(
                        gd1.getLevel().values().toArray(),
                        gd2.getLevel().values().toArray())
        )){
            list.add(GameDataCheck.LEVEL);
        }

        if(!(gd1.getRound() == gd2.getRound() && gd1.getWinningPoints() == gd2.getWinningPoints())){
            list.add(GameDataCheck.OTHERS);
        }

        return list;
    }

    
}