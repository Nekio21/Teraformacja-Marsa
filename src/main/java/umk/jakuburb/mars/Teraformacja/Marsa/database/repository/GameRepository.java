package umk.jakuburb.mars.Teraformacja.Marsa.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import umk.jakuburb.mars.Teraformacja.Marsa.database.entity.Game;
import umk.jakuburb.mars.Teraformacja.Marsa.database.entity.Lobby;

import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {

    Optional<Game> findByUrl(String url);

    @Query("SELECT g.url FROM Game g ORDER BY g.url DESC")
    List<String> getAllURL();
}
