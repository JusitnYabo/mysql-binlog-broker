package com.xpp.mysql.binlog.broker.core;

/**
 * 无锁
 *
 * @author huyapeng
 * @date 2022/2/11
 * Email: yapeng@jspwork.cn
 */
public class MySQLBinlogNonLock implements BinlogLock {


    @Override
    public void lock() {

    }

    @Override
    public void unlock() {

    }

}
