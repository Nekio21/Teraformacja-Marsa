package umk.jakuburb.mars.Teraformacja.Marsa.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import umk.jakuburb.mars.Teraformacja.Marsa.database.entity.Player;

import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByLogin(String login);
}
