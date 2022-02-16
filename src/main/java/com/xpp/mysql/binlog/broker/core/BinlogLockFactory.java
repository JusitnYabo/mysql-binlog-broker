package com.xpp.mysql.binlog.broker.core;

public interface BinlogLockFactory {

    BinlogLock getLock(String lockKey);

}
