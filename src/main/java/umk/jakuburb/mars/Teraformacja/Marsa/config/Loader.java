package umk.jakuburb.mars.Teraformacja.Marsa.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import umk.jakuburb.mars.Teraformacja.Marsa.database.entity.Player;
import umk.jakuburb.mars.Teraformacja.Marsa.database.repository.PlayerRepository;

import java.util.List;

import static umk.jakuburb.mars.Teraformacja.Marsa.database.entity.Player.Role.*;

@Configuration
public class Loader implements ApplicationRunner {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        playerRepository.save(new Player("wru", passwordEncoder.encode("wru"), List.of(USER, LEMONIADA)));
        playerRepository.save(new Player("Norka", passwordEncoder.encode("Norweg"), List.of(USER)));
        playerRepository.save(new Player("PanJaroslaw", passwordEncoder.encode("PanJaroslaw"), List.of(ADMIN)));
    }
}
