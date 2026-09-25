package lk.ac.kln.property_unit_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// Put the new imports up here with the others
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class PropertyUnitServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PropertyUnitServiceApplication.class, args);
	}

	// The Bean method MUST go inside the class's curly braces
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
