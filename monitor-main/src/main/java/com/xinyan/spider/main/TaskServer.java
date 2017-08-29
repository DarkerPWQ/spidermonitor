package com.xinyan.spider.main;

import com.xinyan.spider.monitor.common.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private Logger logger = LoggerFactory.getLogger(TaskServer.class);
    @PostConstruct
    public void start(){
        logger.info(">开始启动[{}]爬虫任务...", Constants.SPIDER_NAME);
        try{
            executorService.execute(spiderServer);
        }catch (Exception e){
            logger.info(">启动[{}]爬虫任务失败.", Constants.SPIDER_NAME, e);
        }
        logger.info(">[{}]爬虫任务已启动.", Constants.SPIDER_NAME);
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
