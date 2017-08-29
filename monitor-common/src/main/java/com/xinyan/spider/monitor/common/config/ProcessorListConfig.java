package com.xinyan.spider.monitor.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
/**
 * @Author：WenqiangPu
 * @Description
 * @Date：Created in 13:58 2017/8/1
 * @Modified By：
 */
@Configuration
public class ProcessorListConfig {
    @Bean
    public List<String> processorList(){
        List<String> processorList = new ArrayList();
        processorList.add("38");
        processorList.add("201");
        return processorList;
    }

}
