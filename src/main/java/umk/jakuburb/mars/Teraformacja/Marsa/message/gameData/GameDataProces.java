package umk.jakuburb.mars.Teraformacja.Marsa.message.gameData;

import org.springframework.stereotype.Component;
import umk.jakuburb.mars.Teraformacja.Marsa.database.entity.Card;
import umk.jakuburb.mars.Teraformacja.Marsa.database.entity.CardSkills;
import umk.jakuburb.mars.Teraformacja.Marsa.message.CardToSend;
import umk.jakuburb.mars.Teraformacja.Marsa.message.Resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameDataProces {



    public static void useCard(String user, Card card, GameData gameData){
        gameData.getMainCards().put(user, Long.valueOf(card.getId()));

        Resources r =  gameData.getResources().get(user);

        for(CardSkills cs: card.getCardSkillsList()){
            boolean plus = cs.getMove()!=CardSkills.Move.LOOSE;
            r.put(cs.getResource(), cs.getAmount(), plus);
        }
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

        if(!(gd1.getRound() == gd2.getRound() && gd1.getCo2() == gd2.getCo2() && gd1.getTemp() == gd2.getTemp() && gd1.getOcean() == gd2.getOcean())){
            list.add(GameDataCheck.OTHERS);
        }

        return list;
    }


}
