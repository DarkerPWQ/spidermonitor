package com.xinyan.spider.monitor.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author：WenqiangPu
 * @Description 配置服务器列表
 * @Date：Created in 13:58 2017/8/1
 * @Modified By：
 */

@Component
@ConfigurationProperties(prefix = "host")
@PropertySource("classpath:application.properties")
public class ProcessorListConfig {
    private List<String> list;

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

}

