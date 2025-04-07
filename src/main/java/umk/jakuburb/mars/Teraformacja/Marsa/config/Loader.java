package umk.jakuburb.mars.Teraformacja.Marsa.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import umk.jakuburb.mars.Teraformacja.Marsa.database.entity.Card;
import umk.jakuburb.mars.Teraformacja.Marsa.database.entity.CardSkills;
import umk.jakuburb.mars.Teraformacja.Marsa.database.entity.Player;
import umk.jakuburb.mars.Teraformacja.Marsa.database.repository.CardRepository;
import umk.jakuburb.mars.Teraformacja.Marsa.database.repository.CardSkillsRepository;
import umk.jakuburb.mars.Teraformacja.Marsa.database.repository.PlayerRepository;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static umk.jakuburb.mars.Teraformacja.Marsa.database.entity.Player.Role.*;

@Configuration
public class Loader implements ApplicationRunner {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CardSkillsRepository cardSkillsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        playerRepository.save(new Player("wru", passwordEncoder.encode("wru"), List.of(USER)));
        playerRepository.save(new Player("Norka", passwordEncoder.encode("Norweg"), List.of(USER)));
        playerRepository.save(new Player("PanJaroslaw", passwordEncoder.encode("PanJaroslaw"), List.of(USER)));
        playerRepository.save(new Player("truskawka", passwordEncoder.encode("majonez"), List.of(USER)));

        saveMainCard("mainCard1.png", List.of(
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.METAL_PROD, 2),
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.ENERGY_PROD, 2),
                new CardSkills(CardSkills.Move.LOOSE, CardSkills.Resource.PLANTS_PROD, 99)
        ));

        saveMainCard("mainCard2.png", List.of(
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.HEAT_PROD, 2),
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.HEAT, 24),
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.TEMP, 3)
        ));

        saveMainCard("mainCard3.png", List.of(
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.GOLD_PROD, 6),
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.HEAT, 3),
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.GOLD, 7)
        ));

        saveMainCard("mainCard4.png", List.of(
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.PLANTS_PROD, 5),
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.PLANTS, 11)
        ));

        saveCard("card1.png",31, Card.TypeCard.BLUE, List.of(
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.HEAT_PROD, 1, CardSkills.Resource.OCEAN)
        ), List.of(Card.Symbol.BACTERIA));
        saveCard("card2.png", 38,Card.TypeCard.BLUE, List.of(
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.ENERGY, 3, CardSkills.Resource.TREE)
        ), List.of(Card.Symbol.BEAR));
        saveCard("card3.png", 21,Card.TypeCard.BLUE, List.of(
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.METAL, 3, CardSkills.Resource.OXYGEN)
        ), List.of(Card.Symbol.EARTH, Card.Symbol.ENERGY));
        saveCard("card4.png", 15,Card.TypeCard.BLUE, List.of(
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.PLANTS, 3, CardSkills.Resource.TEMP)
        ), List.of(Card.Symbol.LEAF, Card.Symbol.LEAF));
        saveCard("card5.png", 22,Card.TypeCard.RED, List.of(
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.HEAT_PROD, 1),
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.TEMP, 2)
        ), List.of(Card.Symbol.ATOM, Card.Symbol.CITY));
        //saveCard("card7.png", 50,Card.TypeCard.GREEN, new ArrayList<>());
        //saveCard("card8.png", 31,Card.TypeCard.BLUE, new ArrayList<>());
        //saveCard("card10.png", 31,Card.TypeCard.BLUE, new ArrayList<>());

        saveCard("card11.png", 21, Card.TypeCard.RED, List.of(
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.HEAT_PROD, 2),
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.ENERGY_PROD, 2)
        ), List.of());
        saveCard("card12.png", 23, Card.TypeCard.RED, List.of(
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.TITANIUM_PROD, 3)
        ), List.of());
        saveCard("card15.png", 1, Card.TypeCard.RED, List.of(
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.ENERGY, 3)
        ), List.of());
        saveCard("card16.png", 1, Card.TypeCard.RED, List.of(
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.HEAT, 3)
        ), List.of());
        saveCard("card17.png", 5, Card.TypeCard.RED, List.of(
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.TEMP, 1)
        ), List.of());
        saveCard("card18.png", 15, Card.TypeCard.RED, List.of(
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.METAL_PROD, 2),
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.ENERGY_PROD, 2)
        ), List.of());
        saveCard("card19.png", 17, Card.TypeCard.RED, List.of(
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.TEMP, 2)
        ), List.of());
        saveCard("card21.png", 12, Card.TypeCard.RED, List.of(
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.OXYGEN, 1)
        ), List.of());
        saveCard("card22.png", 11, Card.TypeCard.RED, List.of(
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.GOLD_PROD, 2)
        ), List.of());
        saveCard("card25.png", 6, Card.TypeCard.RED, List.of(
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.PLANTS_PROD, 3)
        ), List.of());
        saveCard("card26.png", 13, Card.TypeCard.RED, List.of(
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.OCEAN, 1)
        ), List.of());



        saveCard("card27.png", 12, Card.TypeCard.RED, List.of(
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.HEAT, 1),
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.TITANIUM, 1),
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.METAL, 1),
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.ENERGY, 1),
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.PLANTS, 1)
        ), List.of());

        saveCard("card28.png", 14, Card.TypeCard.RED, List.of(
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.PLANTS, 1),
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.PLANTS, 1),
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.PLANTS, 1)
        ), List.of());
        saveCard("card29.png", 13, Card.TypeCard.RED, List.of(
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.TITANIUM, 1),
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.METAL, 1),
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.HEAT, 1)
        ), List.of());
        saveCard("card30.png", 21, Card.TypeCard.RED, List.of(
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.METAL, 3)
        ), List.of());
        saveCard("card31.png", 24, Card.TypeCard.RED, List.of(
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.HEAT, 3)
        ), List.of());
        saveCard("card32.png", 27, Card.TypeCard.RED, List.of(
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.PLANTS, 6)
        ), List.of());
        saveCard("card33.png", 10, Card.TypeCard.RED, List.of(
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.OCEAN, 10),
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.OXYGEN, 10),
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.TEMP, 10)
        ), List.of());

        saveCard("card33.png", 10, Card.TypeCard.RED, List.of(
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.OCEAN, 10),
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.OXYGEN, 10),
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.TEMP, 10)
        ), List.of());

        saveCard("card33.png", 10, Card.TypeCard.RED, List.of(
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.OCEAN, 10),
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.OXYGEN, 10),
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.TEMP, 10)
        ), List.of());

        saveCard("card33.png", 10, Card.TypeCard.RED, List.of(
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.OCEAN, 10),
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.OXYGEN, 10),
                new CardSkills(CardSkills.Move.GET, CardSkills.Resource.TEMP, 10)
        ), List.of());

        for(int i=0;i<10;i++) {
            saveCard("card33.png", 10, Card.TypeCard.RED, List.of(
                    new CardSkills(CardSkills.Move.GET, CardSkills.Resource.OCEAN, 10),
                    new CardSkills(CardSkills.Move.GET, CardSkills.Resource.OXYGEN, 10),
                    new CardSkills(CardSkills.Move.GET, CardSkills.Resource.TEMP, 10)
            ), List.of());
        }

        for(int i=0;i<10;i++) {
            saveCard("card34.png", 1, Card.TypeCard.GREEN, List.of(
                    new CardSkills(CardSkills.Move.GET, CardSkills.Resource.PZ, 3)
            ), List.of(Card.Symbol.CITY, Card.Symbol.CITY, Card.Symbol.ACTION, Card.Symbol.LEAF));
        }

        for(int i=0;i<10;i++) {
            saveCard("card35.png", 5, Card.TypeCard.GREEN, List.of(
                    new CardSkills(CardSkills.Move.GET, CardSkills.Resource.PZ, 1),
                    new CardSkills(CardSkills.Move.GET, CardSkills.Resource.PLANTS_PROD, 3)
            ), List.of(Card.Symbol.CITY, Card.Symbol.ATOM, Card.Symbol.ATOM));
        }

        for(int i=0;i<10;i++) {
            saveCard("card36.png", 11, Card.TypeCard.GREEN, List.of(
                    new CardSkills(CardSkills.Move.GET, CardSkills.Resource.PZ, 2),
                    new CardSkills(CardSkills.Move.GET, CardSkills.Resource.TITANIUM_PROD, 3)
            ), List.of(Card.Symbol.METAL, Card.Symbol.METAL));
        }
        
    }

    private void saveMainCard(String name, List<CardSkills> list) throws IOException {
        saveCard("main/" + name, 0, Card.TypeCard.MAIN, list, List.of());
    }

    private void saveCard(String name, int price, Card.TypeCard typeCard, List<CardSkills> list, List<Card.Symbol> symbolList) throws IOException{
        //File file = new ClassPathResource("static/assets/cards/" + name).getFile();
        //Card card = new Card(typeCard,price, Files.readAllBytes(file.toPath()));

        byte[] fileBytes = new ClassPathResource("static/assets/cards/" + name).getInputStream().readAllBytes();
        Card card = new Card(typeCard, price, fileBytes);


        List<Card.Symbol> listSym = new ArrayList<>();

        for(Card.Symbol symbol: symbolList){
            listSym.add(symbol);
        }

        card.setSymbolList(listSym);

        cardRepository.save(card);

        for(CardSkills cardSkills: list){
            cardSkills.setCard(card);
            cardSkillsRepository.save(cardSkills);
        }
    }
}
