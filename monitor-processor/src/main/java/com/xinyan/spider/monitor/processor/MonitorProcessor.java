package com.xinyan.spider.monitor.processor;

/**
 * @Author：WenqiangPu
 * @Description
 * @Date：Created in 14:22 2017/8/1
 * @Modified By：
 */
public interface MonitorProcessor {
    public void login(String hostName);
    public void monitor(String response);

}
