package umk.jakuburb.mars.Teraformacja.Marsa.controllers;

import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import umk.jakuburb.mars.Teraformacja.Marsa.database.repository.CardRepository;
import umk.jakuburb.mars.Teraformacja.Marsa.rabbit.PlayerQueue;
import umk.jakuburb.mars.Teraformacja.Marsa.utils.MySession;

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

    @GetMapping("/hello")
    public String sayHello(){

        System.out.println("WITAJ");

        return "game";
    }

    @GetMapping("/login")
    public String login(){

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
