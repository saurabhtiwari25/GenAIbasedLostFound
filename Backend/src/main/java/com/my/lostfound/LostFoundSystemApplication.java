package com.my.lostfound;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class LostFoundSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(LostFoundSystemApplication.class, args);
		log.info("Lost & Found System Started Successfully");
	}
}