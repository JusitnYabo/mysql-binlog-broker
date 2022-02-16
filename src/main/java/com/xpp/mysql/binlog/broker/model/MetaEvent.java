package com.xpp.mysql.binlog.broker.model;

import com.github.shyiko.mysql.binlog.event.EventData;
import com.github.shyiko.mysql.binlog.event.TableMapEventData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetaEvent {
    /**
     * 日志数据头文件
     */
    private TableMapEventData meta;
    /**
     * 数据
     */
    private EventData data;
    /**
     * 解析出来的值对象
     */
    private List<BinlogEvent> values;

}
