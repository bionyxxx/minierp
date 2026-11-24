package com.minierp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MinierpApplication {

	public static void main(String[] args) {
		SpringApplication.run(MinierpApplication.class, args);
	}

}
