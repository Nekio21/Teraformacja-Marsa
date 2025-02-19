package umk.jakuburb.mars.Teraformacja.Marsa.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import umk.jakuburb.mars.Teraformacja.Marsa.database.repository.PlayerRepository;
import umk.jakuburb.mars.Teraformacja.Marsa.utils.MyUserDetails;

@Configuration
public class SecurityConfig {

    @Autowired
    private PlayerRepository playerRepository;


    //TODO: potem zrob tak by ten csrf byl dostepny
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
            .authorizeHttpRequests(e->
                e.requestMatchers("/mars/login").anonymous()
                    .requestMatchers("/mars/login/fail").anonymous()
                    .requestMatchers("/mars/register").anonymous()
                    .requestMatchers("/mars/login/check").anonymous()
                    .requestMatchers("/css/**", "/js/**", "/assets/**").permitAll()
                        .anyRequest().authenticated()
        )
            .formLogin(fl ->
                    fl.usernameParameter("login")
                            .passwordParameter("passwd")
                            .loginPage("/mars/login")
                            .loginProcessingUrl("/mars/login/check")
                            .defaultSuccessUrl("/mars/home", true)
                            .failureUrl("/mars/login/fail")
        )
                .logout(l->
                        l.invalidateHttpSession(true)
                                .logoutUrl("/mars/logout")
                                .logoutSuccessUrl("/mars/login")
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public static PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(){
        return new MyUserDetails(playerRepository);
    }

}
