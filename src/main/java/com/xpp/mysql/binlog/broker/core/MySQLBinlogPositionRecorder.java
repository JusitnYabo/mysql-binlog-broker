package com.xpp.mysql.binlog.broker.core;

import com.xpp.mysql.binlog.broker.model.Position;
import com.xpp.mysql.binlog.broker.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

@Slf4j
public class MySQLBinlogPositionRecorder extends TimerTask implements BinlogPositionRecorder {

    /**
     * 使用redis记录处理位置
     */
    private final StringRedisTemplate stringRedisTemplate;
    /**
     * 位置缓冲队列
     */
    private final Queue<Position> queue = new ConcurrentLinkedDeque<>();


    public MySQLBinlogPositionRecorder(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        //  定时器,每100ms上报一次当前处理位置
        Timer timer = new Timer();
        timer.schedule(this, 0, 100);
    }

    @Override
    public Position getLastPosition(String instance) {
        String value = stringRedisTemplate.opsForValue().get(this.key(instance));
        return JsonUtils.fromJson(value, Position.class);
    }

    @Override
    public void record(Position position) {
        //  入列
        queue.offer(position);
    }

    private String key(String instance) {
        return "binlog::position::instance::" + instance;
    }

    @Override
    public void run() {
        if (!queue.isEmpty()) {
            //  位置去重（每个instance只需记录最后一个处理位置即可）
            int size = queue.size();
            Map<String, Position> positionMap = new HashMap<>();
            for (int i = 0; i < size; i++) {
                Position position = queue.poll();
                if (Objects.nonNull(position)) {
                    positionMap.put(position.getName(), position);
                }
            }
            //  记录当前处理位置
            for (String instance : positionMap.keySet()) {
                String position = JsonUtils.toJson(positionMap.get(instance));
                stringRedisTemplate.opsForValue().set(this.key(instance), position);
                if (log.isInfoEnabled()) {
                    log.info("当前解析位置:{}", position);
                }
            }

        }

    }
}
