package com.xpp.mysql.binlog.broker.core;


import com.xpp.mysql.binlog.broker.disruptor.BrokeEventHandler;
import com.xpp.mysql.binlog.broker.disruptor.ParseWorkHandler;
import com.xpp.mysql.binlog.broker.listener.MetaEventListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author huyapeng
 * @date 2022/2/11
 * Email: yapeng@jspwork.cn
 */

@Slf4j
public class MySQLBinlogScheduler extends TimerTask {

    private Map<String, DataSource> dataSources;

    private final Map<String, BinlogBroker> brokers = new ConcurrentHashMap<>();

    private final BinlogLockFactory binlogLockFactory;

    private final StringRedisTemplate stringRedisTemplate;

    private final List<MetaEventListener> eventListeners;

    private final BinlogPositionRecorder binlogPositionRecorder;

    private final Timer timer = new Timer();

    public MySQLBinlogScheduler(Map<String, DataSource> dataSources,
                                BinlogLockFactory binlogLockFactory,
                                StringRedisTemplate stringRedisTemplate,
                                List<MetaEventListener> eventListeners,
                                BinlogPositionRecorder binlogPositionRecorder) {
        this.dataSources = dataSources;
        this.stringRedisTemplate = stringRedisTemplate;
        this.binlogLockFactory = binlogLockFactory;
        this.binlogPositionRecorder = binlogPositionRecorder;
        this.eventListeners = eventListeners;
    }

    public void schedule(long delay, long period) {
        timer.schedule(this, delay, period);
    }

    @Override
    public void run() {
        try {
            notifyWorkers();
        } catch (Exception e) {
            log.error("工作器定时任务调度异常", e);
        }
    }

    public void notifyWorkers() {

        //  检查worker
        checkBrokers();

        //  尝试添加新的worker
        addBrokers();

    }


    /**
     * 尝试添加新的worker
     */
    private void addBrokers() {

        dataSources.forEach((key, value) -> {
            if (!brokers.containsKey(key)) {
                this.addBroker(key, value);
            }
        });

    }

    /**
     * 添加新的broker
     *
     * @param name       分发器名称
     * @param dataSource 数据源
     */
    private void addBroker(String name, DataSource dataSource) {
        //  获取锁
        BinlogLock lock = binlogLockFactory.getLock(name);
        if (Objects.nonNull(lock)) {

            BinlogEventPublisher eventPublisher = createEventPublisher(dataSource);
            //  新建工作器
            BinlogBroker broker = new MySQLBinlogBroker(name, dataSource, lock, eventPublisher, binlogPositionRecorder);
            //  开启工作器
            broker.start();
            //  添加到工作池
            brokers.put(name, broker);

            log.info("add broker {}", name + "-broker");
        }
    }

    private MySQLBinlogEventPublisher createEventPublisher(DataSource dataSource) {

        BinlogDataStructureManager binlogDataStructureManager = new MySQLBinlogDataStructureManager(dataSource, stringRedisTemplate);
        BinlogParser binlogParser = new MySQLBinlogParser(binlogDataStructureManager);

        //  按照核心数设置解析线程,并行处理
        int coreSize = Runtime.getRuntime().availableProcessors();

        ParseWorkHandler[] parseWorkHandlers = new ParseWorkHandler[coreSize];
        for (int i = 0; i < parseWorkHandlers.length; i++) {
            parseWorkHandlers[i] = new ParseWorkHandler(binlogParser);
        }

        BrokeEventHandler[] eventHandlers = new BrokeEventHandler[]{new BrokeEventHandler(eventListeners)};

        return new MySQLBinlogEventPublisher(parseWorkHandlers, eventHandlers);

    }


    /**
     * 移除失活的broker
     */
    private void checkBrokers() {
        brokers.keySet().forEach(key -> {
            if (!dataSources.containsKey(key)) {
                this.removeBroker(key);
            }
        });
    }


    private void removeBroker(String name) {
        BinlogBroker broker = brokers.get(name);
        if (Objects.nonNull(broker)) {
            //  关闭处理器
            broker.stop();
            //  从工作池中移除处理器
            brokers.remove(name);

            log.info("remove broker {}", name + "-broker");
        }
    }


}
