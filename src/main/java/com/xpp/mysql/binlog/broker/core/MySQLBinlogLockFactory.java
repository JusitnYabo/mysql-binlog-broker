package com.xpp.mysql.binlog.broker.core;

import com.xpp.mysql.binlog.broker.utils.IpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MySQLBinlogLockFactory implements BinlogLockFactory {

    private final static String LOCK_PREFIX = "binlog::lock::";

    private final StringRedisTemplate stringRedisTemplate;

    public MySQLBinlogLockFactory(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public BinlogLock getLock(String lockKey) {
        String key = LOCK_PREFIX + lockKey;
        String value = IpUtils.getIpAddress() + ":" + UUID.randomUUID().toString();
        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(key, value, 1, TimeUnit.MINUTES);
        if (Boolean.TRUE.equals(locked)) {
            log.info("get lock {} success with value {}", key, value);
            return new MySQLBinlogLock(key, value, stringRedisTemplate);
        } else {
            log.info("get lock {} failed with value {}", key, value);
            return null;
        }

    }
}
