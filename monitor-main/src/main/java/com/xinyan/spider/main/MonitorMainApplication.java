package com.xinyan.spider.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.xinyan.spider")
public class MonitorMainApplication {

	public static void main(String[] args) {
		SpringApplication.run(MonitorMainApplication.class, args);
	}
}
