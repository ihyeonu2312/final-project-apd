package site.unoeyhi.apd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {
    org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration.class
})
@EnableScheduling
public class ApdApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApdApplication.class, args);
	}

}
