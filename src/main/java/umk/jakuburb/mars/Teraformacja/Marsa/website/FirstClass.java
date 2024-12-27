package umk.jakuburb.mars.Teraformacja.Marsa.website;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RequestMapping("/mars")
public class FirstClass {

    @GetMapping("/hello")
    public String sayHello(){
        return "game";
    }


}
