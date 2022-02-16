package com.xpp.mysql.binlog.broker.core;

/**
 * Binlog日志锁,防止Binlog日志重复读取
 */
public interface BinlogLock {

    void lock();

    void unlock();
}
