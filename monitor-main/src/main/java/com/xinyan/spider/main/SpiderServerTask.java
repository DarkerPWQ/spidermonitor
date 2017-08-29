package com.xinyan.spider.main;

import com.gargoylesoftware.htmlunit.WebClient;
import com.xinyan.spider.monitor.common.config.ProcessorListConfig;
import com.xinyan.spider.monitor.common.utils.Constants;
import com.xinyan.spider.monitor.common.utils.NamedThreadFactory;
import com.xinyan.spider.monitor.processor.MonitorProcessor;
import com.xinyan.spider.monitor.processor.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author：WenqiangPu
 * @Description
 * @Date：Created in 11:09 2017/8/1
 * @Modified By：
 */
@Component
public class SpiderServerTask implements Runnable,ApplicationContextAware {

    private Logger logger = LoggerFactory.getLogger(SpiderServerTask.class);
    private ApplicationContext applicationContext;
    @Value("${host.body}")
    private String body;
    @Value("${host.number}")
    private int hostNumber;
    private ExecutorService executorService;

    @Autowired
    private ProcessorListConfig processorListConfig;//配置类

    @Override
    public void run() {
        logger.info(">开始接收[{}]任务...", Constants.SPIDER_NAME);
        executorService = Executors.newFixedThreadPool(hostNumber, new NamedThreadFactory());
        List<String> list = processorListConfig.getList();
        try{
            for(String host:list){
                logger.info(">从爬虫配置获取[{}]主机任务...", host);
                executeMonitorWork(host);
            }
        }catch (Exception e){
            logger.error(">从爬虫配置获取主机任务失败...");
        }

    }
    private void executeMonitorWork(String host) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                logger.info(">[{}]主机开始执行登陆任务...",host);
                try {
                    Processor processor =  (Processor)applicationContext.getBean("processor");
                    processor.setHostName(host);
                    processor.setBody(body);
                    processor.login();
                } catch (Throwable ex) {
                    logger.error(">[{}]主机开始执行任务失败...",host);
                }finally {
                    logger.error(">[{}]主机任务退出...",host);
                }
            }
        });
    }
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = applicationContext;
    }
    public void stop() {
        executorService.shutdown();
    }
}
