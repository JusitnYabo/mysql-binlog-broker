package com.xpp.mysql.binlog.broker.core;


import com.xpp.mysql.binlog.broker.model.MetaEvent;
import com.xpp.mysql.binlog.broker.model.BinlogEvent;

import java.util.List;

public interface BinlogParser {

    /**
     * @param event 元数据事件
     * @return 
     */
    List<BinlogEvent> parse(MetaEvent event);

}
