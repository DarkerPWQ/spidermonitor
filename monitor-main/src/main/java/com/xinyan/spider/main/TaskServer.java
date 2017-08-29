package com.xinyan.spider.main;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author：WenqiangPu
 * @Description
 * @Date：Created in 11:06 2017/8/1
 * @Modified By：
 */
@Component
@Configuration
public class TaskServer {
    private ExecutorService executorService = Executors.newFixedThreadPool(1);
    @Autowired
    private SpiderServerTask spiderServer;

    @PostConstruct
    public void start(){
        try{
            executorService.execute(spiderServer);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @PreDestroy
    public void stop(){
        try{
            spiderServer.stop();
            executorService.shutdown();
        }catch (Throwable ex){
        }
    }
}
