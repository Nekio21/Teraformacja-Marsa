package umk.jakuburb.mars.Teraformacja.Marsa;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mars")
public class FirstClass {
    @GetMapping("/hello")
    public String sayHello(){
        return "Siemaaaa!!!!!";
    }
}
