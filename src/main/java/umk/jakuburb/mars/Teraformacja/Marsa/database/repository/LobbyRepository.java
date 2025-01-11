package umk.jakuburb.mars.Teraformacja.Marsa.database.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import umk.jakuburb.mars.Teraformacja.Marsa.database.entity.Lobby;

import java.util.List;
import java.util.Optional;

@Repository
public interface LobbyRepository extends JpaRepository<Lobby, Long> {

    Optional<Lobby> findByUrl(String url);

    Optional<Lobby> findByCode(String code);

    @Query("SELECT l.code FROM Lobby l ORDER BY l.code DESC")
    List<String> getAllCode();

    @Query("SELECT l.url FROM Lobby l ORDER BY l.url DESC")
    List<String> getAllURL();
}
