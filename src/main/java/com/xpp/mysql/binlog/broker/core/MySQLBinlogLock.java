package com.xpp.mysql.binlog.broker.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * @author huyapeng
 * @date 2022/2/11
 * Email: yapeng@jspwork.cn
 */
@Slf4j
public class MySQLBinlogLock implements BinlogLock {

    private final String key;

    private final String value;

    private final StringRedisTemplate stringRedisTemplate;

    private Timer timer;

    public MySQLBinlogLock(String key, String value, StringRedisTemplate stringRedisTemplate) {
        this.key = key;
        this.value = value;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public synchronized void lock() {
        //  当前是否持有锁
        if (this.hasLock()) {
            //  当前持有锁，开启定时任务续租
            startExpireTimer();
        } else {
            //  当前未持有锁，尝试加锁
            stringRedisTemplate.opsForValue().setIfAbsent(key, value, 1, TimeUnit.MINUTES);
        }

        //  最后检查是否真的拿到了锁
        if (!this.hasLock()) {
            throw new IllegalArgumentException("locked failed");
        }

    }

    /**
     * 开启续租定时任务
     */
    private void startExpireTimer() {
        this.timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                stringRedisTemplate.expire(key, 1, TimeUnit.MINUTES);
                log.info("expire lock {}", key);
            }
        }, 0, 10000);
    }

    @Override
    public synchronized void unlock() {
        //  并没有真实删除锁，只是关闭定时任务续租，让锁自然释放
        if (Objects.nonNull(timer)) {
            timer.cancel();
        }

    }

    private boolean hasLock() {
        return this.value.equals(stringRedisTemplate.opsForValue().get(key));
    }

}
