package com.aviva.appointmentsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AppointmentsystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppointmentsystemApplication.class, args);
	}
}
