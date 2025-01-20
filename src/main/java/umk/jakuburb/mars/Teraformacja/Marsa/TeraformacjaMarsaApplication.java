package umk.jakuburb.mars.Teraformacja.Marsa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TeraformacjaMarsaApplication {

	public static void main(String[] args) {
		SpringApplication.run(TeraformacjaMarsaApplication.class, args);
	}

}
