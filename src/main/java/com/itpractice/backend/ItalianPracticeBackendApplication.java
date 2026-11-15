package com.itpractice.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Locale;
import java.util.TimeZone;

@SpringBootApplication
public class ItalianPracticeBackendApplication {

	public static void main(String[] args) {

		Locale.setDefault(Locale.ROOT);
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		SpringApplication.run(ItalianPracticeBackendApplication.class, args);
	}

}
