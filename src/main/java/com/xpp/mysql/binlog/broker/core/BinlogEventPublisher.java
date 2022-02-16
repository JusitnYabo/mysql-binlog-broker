package com.xpp.mysql.binlog.broker.core;


import com.xpp.mysql.binlog.broker.model.MetaEvent;

public interface BinlogEventPublisher {

    void publish(MetaEvent event);


}
