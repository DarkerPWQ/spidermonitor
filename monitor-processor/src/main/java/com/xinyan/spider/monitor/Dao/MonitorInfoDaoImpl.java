package com.xinyan.spider.monitor.Dao;

import com.xinyan.spider.monitor.model.MonitorInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * @Author：WenqiangPu
 * @Description
 * @Date：Created in 15:11 2017/8/4
 * @Modified By：
 */
@Repository
public class MonitorInfoDaoImpl implements MonitorInfoDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public int add(MonitorInfo monitorInfo) {
        return jdbcTemplate.update("insert into t_spider_server_monitor_info(host_name,start_time,end_time,IOPS,bytes)VALUES(?,?,?,?,?)",monitorInfo.getHostName(),monitorInfo.getStartTime(),monitorInfo.getEndTime(),monitorInfo.getIops(),monitorInfo.getBytes());
    }
}
