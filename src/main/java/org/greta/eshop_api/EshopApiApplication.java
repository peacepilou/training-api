package org.greta.eshop_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class EshopApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(EshopApiApplication.class, args);
	}

	@GetMapping("/hello")
	public String sayHello() {
		return "Hello World!";
	}

	@GetMapping("/am-i-a-warthog")
	public String amIAWarthog() {
		return "Yes, of course!";
	}

}
