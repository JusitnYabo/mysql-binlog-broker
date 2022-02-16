package com.xpp.mysql.binlog.broker.core;


import com.xpp.mysql.binlog.broker.model.Position;

/**
 * binlog日志位置记录器
 */
public interface BinlogPositionRecorder {

    Position getLastPosition(String instance);

    void record(Position position);


}
