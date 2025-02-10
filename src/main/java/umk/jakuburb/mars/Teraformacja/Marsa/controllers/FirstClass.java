package umk.jakuburb.mars.Teraformacja.Marsa.controllers;

import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import umk.jakuburb.mars.Teraformacja.Marsa.database.entity.Player;
import umk.jakuburb.mars.Teraformacja.Marsa.database.repository.CardRepository;
import umk.jakuburb.mars.Teraformacja.Marsa.database.repository.PlayerRepository;
import umk.jakuburb.mars.Teraformacja.Marsa.rabbit.PlayerQueue;
import umk.jakuburb.mars.Teraformacja.Marsa.utils.MySession;

import java.util.List;

@Controller
@RequestMapping("/mars")
public class FirstClass {

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private MySession mySession;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/hello")
    public String sayHello(){

        System.out.println("WITAJ");

        return "game";
    }

    @GetMapping("/register")
    public String register(@RequestParam(value="confirm", required=false, defaultValue = "true") String confirm, Model model){

        if(confirm.equals("false")){
            model.addAttribute("c", false);
        }else{
            model.addAttribute("c", true);
        }

        return "register";
    }

    @PostMapping("/register")
    public String registerPost(String login, String passwd){
        if(playerRepository.findByLogin(login).isEmpty()){
            Player player = new Player();
            player.setLogin(login);
            player.setPasswd(passwordEncoder.encode(passwd));
            player.setRole(List.of(Player.Role.USER));
            playerRepository.save(player);
        }else{
            return "redirect:/mars/register?confirm=false";
        }

        return "redirect:/mars/login?confirm=true";
    }

    @GetMapping("/login")
    public String login(@RequestParam(value="confirm", required=false, defaultValue = "false") String confirm, Model model){

        if(confirm.equals("false")){
            model.addAttribute("c", false);
        }else{
            model.addAttribute("c", true);
        }

        System.out.println("Login");

        return "login";
    }

    @GetMapping("/login/fail")
    @ResponseBody
    public String fail(){

        System.out.println("fail");

        return "fail";
    }

    @GetMapping("/error")
    @ResponseBody
    public String error(){
        return "error";
    }

    @GetMapping("/home")
    public String homePage(Model model){
        UserDetails playerInfo = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String s = playerInfo.getUsername();

        if(mySession.getPlayerQueue() == null) {
            PlayerQueue playerQueue = new PlayerQueue(s,cardRepository, rabbitAdmin, rabbitTemplate);
            mySession.setPlayerQueue(playerQueue);
        }
        else if(!mySession.getPlayerQueue().getUniqName().equals(s)){
            PlayerQueue playerQueue = new PlayerQueue(s, cardRepository, rabbitAdmin, rabbitTemplate);
            mySession.clear();
            mySession.setPlayerQueue(playerQueue);
        }

        model.addAttribute("name", s);

        return "homePage";
    }
}
