package com.jason7599.postnaut;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PostnautApplication {

	public static void main(String[] args) {
		SpringApplication.run(PostnautApplication.class, args);
	}

}
