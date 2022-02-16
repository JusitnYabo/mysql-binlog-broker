package com.xpp.mysql.binlog.broker.disruptor;

import com.lmax.disruptor.WorkHandler;
import com.xpp.mysql.binlog.broker.core.BinlogParser;
import com.xpp.mysql.binlog.broker.model.MetaEvent;
import com.xpp.mysql.binlog.broker.model.BinlogEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * 解析工作处理器
 */
@Slf4j
public class ParseWorkHandler implements WorkHandler<MetaEvent> {

    private final BinlogParser binlogParser;

    public ParseWorkHandler(BinlogParser binlogParser) {
        this.binlogParser = binlogParser;
    }

    @Override
    public void onEvent(MetaEvent event) throws Exception {

        //  解析binlog事件
        List<BinlogEvent> rowEvents = binlogParser.parse(event);
        //  包装values
        if (CollectionUtils.isEmpty(rowEvents)) {
            event.setValues(Collections.emptyList());
        } else {
            event.setValues(rowEvents);
        }

    }
}
