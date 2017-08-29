package com.xinyan.spider.main;

import com.xinyan.spider.monitor.common.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@ComponentScan(basePackages = "com.xinyan.spider")
//@PropertySource(value = {"file:/data/xinyan/spider/config/spider-serverMonitor.properties"})
public class MonitorMainApplication {
	private static final Logger logger = LoggerFactory.getLogger(MonitorMainApplication.class);
	public static void main(String[] args) {

		logger.info(">正在启动[{}]程序...", Constants.SPIDER_NAME);
		SpringApplication.run(MonitorMainApplication.class, args);
	}
}
