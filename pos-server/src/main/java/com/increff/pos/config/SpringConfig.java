package com.increff.pos.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootApplication
@ComponentScan(basePackages = { "com.increff.pos" })
@EnableScheduling
public class SpringConfig {

	public static void main(String[] args) {
		SpringApplication.run(SpringConfig.class, args);
	}
}