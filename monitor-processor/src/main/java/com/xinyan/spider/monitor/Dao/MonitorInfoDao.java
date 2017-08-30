package com.xinyan.spider.monitor.Dao;

import com.xinyan.spider.monitor.model.MonitorInfo;

/**
 * @Author：WenqiangPu
 * @Description
 * @Date：Created in 15:09 2017/8/4
 * @Modified By：
 */
public interface MonitorInfoDao {
    //在master上修改
    int add(MonitorInfo monitorInfo);
}
