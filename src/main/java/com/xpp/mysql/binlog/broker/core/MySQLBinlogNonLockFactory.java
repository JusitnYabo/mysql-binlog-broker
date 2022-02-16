package com.xpp.mysql.binlog.broker.core;

import lombok.extern.slf4j.Slf4j;

/**
 * 无锁工厂
 */
@Slf4j
public class MySQLBinlogNonLockFactory implements BinlogLockFactory {

    @Override
    public BinlogLock getLock(String lockKey) {
        return new MySQLBinlogNonLock();
    }

}
