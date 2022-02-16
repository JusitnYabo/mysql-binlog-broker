package com.xpp.mysql.binlog.broker.disruptor;

import com.xpp.mysql.binlog.broker.model.MetaEvent;
import com.lmax.disruptor.EventFactory;

public class MetaEventFactory implements EventFactory<MetaEvent> {
    @Override
    public MetaEvent newInstance() {
        return new MetaEvent();
    }
}
