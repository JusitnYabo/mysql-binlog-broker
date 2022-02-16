package com.xpp.mysql.binlog.broker.core;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.*;
import com.xpp.mysql.binlog.broker.model.MetaEvent;
import com.xpp.mysql.binlog.broker.model.Position;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 单个database处理器
 *
 * @author huyapeng
 * @date 2022/2/11
 * Email: yapeng@jspwork.cn
 */
@Slf4j
public class MySQLBinlogBroker implements BinlogBroker, BinaryLogClient.EventListener, Runnable {

    private final String name;
    private Thread thread = null;
    private RegexFilter filter;
    private final BinaryLogClient binaryLogClient;
    private final DataSource dataSource;
    private final BinlogLock binlogLock;
    private final BinlogEventPublisher binlogEventPublisher;
    private final BinlogPositionRecorder binlogPositionRecorder;
    private TableMapEventData currentTable;

    public MySQLBinlogBroker(String name, DataSource dataSource, BinlogLock binlogLock, BinlogEventPublisher binlogEventPublisher, BinlogPositionRecorder binlogPositionRecorder) {
        this.name = name;
        this.dataSource = dataSource;
        this.binlogLock = binlogLock;
        this.binlogEventPublisher = binlogEventPublisher;
        this.binlogPositionRecorder = binlogPositionRecorder;
        this.binaryLogClient = createClient();
    }

    private BinaryLogClient createClient() {
        Map<String, Object> jdbcProperties = getJdbcProperties();
        String host = (String) jdbcProperties.get("host");
        Integer port = (Integer) jdbcProperties.get("port");
        String username = (String) jdbcProperties.get("username");
        String password = (String) jdbcProperties.get("password");

        BinaryLogClient binaryLogClient = new BinaryLogClient(host, port, username, password);
        binaryLogClient.registerEventListener(this);
        Position position = binlogPositionRecorder.getLastPosition(this.name);
        if (Objects.nonNull(position)) {
            binaryLogClient.setBinlogFilename(position.getFile());
            binaryLogClient.setBinlogPosition(position.getOffset());
        }
        return binaryLogClient;
    }

    private Map<String, Object> getJdbcProperties() {
        Map<String, Object> properties = new HashMap<>();
        try {
            if (dataSource instanceof HikariDataSource) {
                properties.put("username", ((HikariDataSource) dataSource).getUsername());
                properties.put("password", ((HikariDataSource) dataSource).getPassword());
                Map<String, Object> jdbcProperties = getPropertiesFromJdbcUrl(((HikariDataSource) dataSource).getJdbcUrl());
                properties.putAll(jdbcProperties);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return properties;

    }


    /**
     * 从jdbc url中提取属性
     * example: jdbc:mysql://localhost:3306/test?characterEncoding=utf8&useSSL=false&serverTimezone=CTT
     *
     * @param url jdbc url
     * @return 属性
     */
    private Map<String, Object> getPropertiesFromJdbcUrl(String url) {
        Map<String, Object> result = new HashMap<>();
        if (StringUtils.hasText(url) && url.startsWith("jdbc")) {
            //  去除协议头
            if (url.contains("//")) {
                url = url.substring(url.indexOf("//") + 2);
            }
            if (url.contains("/")) {
                url = url.substring(0, url.indexOf("/"));
            }
            if (url.contains("?")) {
                url = url.substring(0, url.indexOf("?"));
            }

            if (url.contains(":")) {
                String[] strs = url.split(":");
                if (strs.length > 1) {
                    String host = strs[0];
                    if (StringUtils.hasText(host)) {
                        result.put("host", host);
                    }
                    String port = strs[1];
                    if (NumberUtils.isCreatable(port)) {
                        result.put("port", Integer.valueOf(port));
                    }

                }
            }


        }
        return result;
    }


    @Override
    public synchronized void start() {
        if (Objects.isNull(thread)) {
            //  加锁独占
            binlogLock.lock();
            // 开启工作线程
            thread = new Thread(this);
            thread.setName(this.name + "-broker");
            thread.start();
        }
    }


    @Override
    public void stop() {
        try {
            //  断开连接
            if (binaryLogClient.isConnected()) {
                binaryLogClient.disconnect();
            }
            //  关闭线程
            if (thread.isAlive()) {
                thread.interrupt();
            }
            //  释放锁
            binlogLock.unlock();

        } catch (Exception e) {
            log.error("关闭客户端异常", e);
        }


    }

    @Override
    public void onEvent(Event event) {

        EventData data = event.getData();
        if (data instanceof TableMapEventData) {
            //  当前操作表
            currentTable = (TableMapEventData) data;
            return;
        }

        if (isValidData(data)) {
            MetaEvent metaEvent = new MetaEvent();
            metaEvent.setMeta(currentTable);
            metaEvent.setData(data);

            //  发布事件
            binlogEventPublisher.publish(metaEvent);
        }

        //  记录当前位置
        Position position = new Position(name, binaryLogClient.getBinlogFilename(), binaryLogClient.getBinlogPosition());
        binlogPositionRecorder.record(position);
    }


    private boolean isValidData(EventData data) {
        if (Objects.nonNull(currentTable)) {
            boolean accept = Objects.isNull(filter) || filter.accept(currentTable.getDatabase(), currentTable.getTable());
            if (accept) {

                if (data instanceof WriteRowsEventData) {
                    return ((WriteRowsEventData) data).getTableId() == currentTable.getTableId();
                }

                if (data instanceof UpdateRowsEventData) {
                    return ((UpdateRowsEventData) data).getTableId() == currentTable.getTableId();
                }

                if (data instanceof DeleteRowsEventData) {
                    return ((DeleteRowsEventData) data).getTableId() == currentTable.getTableId();
                }

            }

        }

        return false;

    }


    @Override
    public void run() {
        try {
            if (!binaryLogClient.isConnected()) {
                binaryLogClient.connect();
            }
        } catch (Exception e) {
            log.error(this.name + "客户端连接异常");
            throw new RuntimeException(e);
        }
    }
}
