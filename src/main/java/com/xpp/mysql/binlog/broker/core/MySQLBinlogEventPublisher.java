package com.xpp.mysql.binlog.broker.core;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.xpp.mysql.binlog.broker.disruptor.MetaEventFactory;
import com.xpp.mysql.binlog.broker.model.MetaEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.Collections;

@Slf4j
public class MySQLBinlogEventPublisher implements BinlogEventPublisher {


    /**
     * 环形缓冲区大小
     */
    private final int DEFAULT_BUFFER_SIZE = 1024;

    private final Disruptor<MetaEvent> disruptor;


    public MySQLBinlogEventPublisher(WorkHandler<MetaEvent>[] workHandlers,
                                     EventHandler<MetaEvent>[] eventHandlers) {
        this.disruptor = new Disruptor<>(new MetaEventFactory(), DEFAULT_BUFFER_SIZE, new CustomizableThreadFactory("event-handler-"), ProducerType.MULTI, new BlockingWaitStrategy());
        this.disruptor.handleEventsWithWorkerPool(workHandlers).then(eventHandlers);
        this.disruptor.start();
    }


    @Override
    public void publish(MetaEvent event) {
        RingBuffer<MetaEvent> ringBuffer = disruptor.getRingBuffer();
        // 获取下一个Event槽的下标
        long sequence = ringBuffer.next();
        try {
            // 填充数据
            MetaEvent sequenceEvent = ringBuffer.get(sequence);
            sequenceEvent.setMeta(event.getMeta());
            sequenceEvent.setData(event.getData());
            sequenceEvent.setValues(Collections.emptyList());
        } catch (Exception e) {
            log.error("failed to set event data to ringBuffer:", e);
        } finally {
            // 发布Event，激活观察者去消费，将sequence传递给改消费者
            ringBuffer.publish(sequence);
        }

    }
}
