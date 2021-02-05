package com.wang.business;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@SpringBootApplication()
@EnableAsync
public class Application {
    public static void main(String[] args) {
    	try {
    		SpringApplication.run(Application.class, args);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
        
    }
}
