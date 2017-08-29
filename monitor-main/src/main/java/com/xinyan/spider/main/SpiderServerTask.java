package com.xinyan.spider.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author：WenqiangPu
 * @Description
 * @Date：Created in 11:09 2017/8/1
 * @Modified By：
 */
@Component
public class SpiderServerTask implements Runnable,ApplicationContextAware {

    private Logger logger = LoggerFactory.getLogger(TaskServer.class);
    private ApplicationContext applicationContext;
    @Value("${author.body}")
    private String body;
    @Value("${host.number}")
    private int hostName;

    @Autowired
    private List<String> processorList;//监控服务器列表

    @Override
    public void run() {



    }

    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = applicationContext;
    }
    public void stop() {
        System.out.println("stop");

    }
}
