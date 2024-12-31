package umk.jakuburb.mars.Teraformacja.Marsa.website;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/mars")
public class FirstClass {

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

    @GetMapping("/home")
    public String homePage(Model model){
        UserDetails playerInfo = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String s = playerInfo.getUsername();

        model.addAttribute("name", s);

        return "homePage";
    }
}
