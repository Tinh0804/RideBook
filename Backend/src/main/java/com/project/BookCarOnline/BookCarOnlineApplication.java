package com.project.BookCarOnline;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BookCarOnlineApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookCarOnlineApplication.class, args);
	}

}
