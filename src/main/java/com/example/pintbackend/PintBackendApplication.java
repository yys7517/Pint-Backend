package com.example.pintbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class PintBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PintBackendApplication.class, args);
	}

}
