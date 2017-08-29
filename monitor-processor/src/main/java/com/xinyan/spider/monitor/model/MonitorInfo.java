package com.xinyan.spider.monitor.model;

/**
 * @Author：WenqiangPu
 * @Description
 * @Date：Created in 14:19 2017/8/1
 * @Modified By：
 */
public class MonitorInfo {
    private String startTime;
    private String endTime;
    private String iops;
    private String bytes;
    private String hostName;

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getIops() {
        return iops;
    }

    public void setIops(String iops) {
        this.iops = iops;
    }

    public String getBytes() {
        return bytes;
    }

    public void setBytes(String bytes) {
        this.bytes = bytes;
    }
}
