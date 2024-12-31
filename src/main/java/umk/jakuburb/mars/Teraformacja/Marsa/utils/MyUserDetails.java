package umk.jakuburb.mars.Teraformacja.Marsa.utils;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import umk.jakuburb.mars.Teraformacja.Marsa.database.entity.Player;
import umk.jakuburb.mars.Teraformacja.Marsa.database.repository.PlayerRepository;

import java.util.List;
import java.util.stream.Collectors;

public class MyUserDetails implements UserDetailsService {

    private PlayerRepository playerRepository;

    public MyUserDetails(PlayerRepository playerRepository){
        this.playerRepository = playerRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Player player = playerRepository.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        List<Player.Role> uprawnienia = player.getRole();

        List<GrantedAuthority> uprawnieniaLista = uprawnienia
                .stream()
                .map(e->new SimpleGrantedAuthority(e.name()))
                .collect(Collectors.toList());

        return new User(username, player.getPasswd(), uprawnieniaLista);
    }
}
