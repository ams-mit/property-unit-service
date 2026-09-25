package lk.ac.kln.property_unit_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@Bean
public RestTemplate restTemplate() {
    return new RestTemplate();
}

@SpringBootApplication
public class PropertyUnitServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PropertyUnitServiceApplication.class, args);
	}
}
