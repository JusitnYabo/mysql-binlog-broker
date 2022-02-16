package com.xpp.mysql.binlog.broker.disruptor;

import com.github.shyiko.mysql.binlog.event.TableMapEventData;
import com.lmax.disruptor.EventHandler;
import com.xpp.mysql.binlog.broker.listener.MetaEventListener;
import com.xpp.mysql.binlog.broker.model.MetaEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 分发事件处理器
 */
@Slf4j
public class BrokeEventHandler implements EventHandler<MetaEvent> {

    /**
     * 监听器列表
     */
    private final List<MetaEventListener> eventListeners;
    /**
     * 事件分发监听组
     */
    private final Map<String, List<MetaEventListener>> eventListenersGroup = new ConcurrentHashMap<>();

    public BrokeEventHandler(List<MetaEventListener> eventListeners) {
        this.eventListeners = eventListeners;
    }

    @Override
    public void onEvent(MetaEvent event, long sequence, boolean endOfBatch) throws Exception {

        //  获取对应的监听组
        List<MetaEventListener> eventListeners = getEventListeners(event.getMeta());

        //  处理事件
        eventListeners.forEach(e -> {
            try {
                e.onEvent(event);
            } catch (Exception exception) {
                log.error("处理错误:", exception);
            }
        });

    }

    private List<MetaEventListener> getEventListeners(TableMapEventData meta) {
        String key = meta.getDatabase() + "." + meta.getTable();
        List<MetaEventListener> metaEventListeners = eventListenersGroup.get(key);
        if (Objects.isNull(metaEventListeners)) {
            metaEventListeners = eventListeners.stream().filter(e -> e.subscribe(meta.getDatabase(), meta.getTable())).collect(Collectors.toList());
            eventListenersGroup.put(key, eventListeners);
        }
        return metaEventListeners;

    }
}
