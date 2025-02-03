package umk.jakuburb.mars.Teraformacja.Marsa.database.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import umk.jakuburb.mars.Teraformacja.Marsa.database.entity.Card;

import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    @Query("SELECT c FROM Card c WHERE c.typeCard = MAIN ORDER BY random() LIMIT 3")
    List<Card> getRandomMainCard();

    @Query("SELECT c FROM Card c WHERE c.typeCard != MAIN AND c.id NOT IN :draw ORDER BY random() LIMIT :much")
    List<Card> getRandomCards(@Param("draw")List<Long> drawCards, @Param("much") int much);

    @Query("SELECT c FROM Card c WHERE c.id IN :ids")
    List<Card> getCards(@Param("ids")List<Long> ids);

    @Query("SELECT c FROM Card c WHERE c.id = :id")
    Card getCard(@Param("id")Long ids);
}
