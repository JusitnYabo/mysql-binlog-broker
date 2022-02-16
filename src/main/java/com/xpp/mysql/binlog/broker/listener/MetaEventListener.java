package com.xpp.mysql.binlog.broker.listener;


import com.xpp.mysql.binlog.broker.model.MetaEvent;

public interface MetaEventListener {

    /**
     * @param database 数据库名
     * @param table    表名
     * @return 是否订阅
     */
    boolean subscribe(String database, String table);

    void onEvent(MetaEvent event);

}
