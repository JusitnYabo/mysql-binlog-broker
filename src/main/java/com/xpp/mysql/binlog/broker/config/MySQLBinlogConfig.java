package com.xpp.mysql.binlog.broker.config;

import com.xpp.mysql.binlog.broker.core.*;
import com.xpp.mysql.binlog.broker.listener.MetaEventListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@Slf4j
@Data
@Configuration
public class MySQLBinlogConfig {

    /**
     * 多实例支持只需要配置多个数据源即可
     */
    @Resource
    private Map<String, DataSource> dataSources;

    @Bean
    public MySQLBinlogScheduler mySQLBinlogScheduler(StringRedisTemplate stringRedisTemplate, List<MetaEventListener> eventListeners) {

        BinlogLockFactory binlogLockFactory = new MySQLBinlogLockFactory(stringRedisTemplate);

        BinlogPositionRecorder binlogPositionRecorder = new MySQLBinlogPositionRecorder(stringRedisTemplate);
        //  构建日志任务调度器
        MySQLBinlogScheduler scheduler = new MySQLBinlogScheduler(dataSources, binlogLockFactory, stringRedisTemplate, eventListeners, binlogPositionRecorder);
        //  开启调度
        scheduler.schedule(0, 1000);

        return scheduler;
    }


}
