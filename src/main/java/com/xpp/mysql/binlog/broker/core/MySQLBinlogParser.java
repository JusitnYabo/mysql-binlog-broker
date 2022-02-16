package com.xpp.mysql.binlog.broker.core;

import com.github.shyiko.mysql.binlog.event.*;
import com.xpp.mysql.binlog.broker.enums.EventType;
import com.xpp.mysql.binlog.broker.model.Column;
import com.xpp.mysql.binlog.broker.model.MetaEvent;
import com.xpp.mysql.binlog.broker.model.BinlogEvent;
import com.xpp.mysql.binlog.broker.model.Table;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.*;

@Slf4j
public class MySQLBinlogParser implements BinlogParser {

    private final BinlogDataStructureManager dataStructureManager;

    public MySQLBinlogParser(BinlogDataStructureManager dataStructureManager) {
        this.dataStructureManager = dataStructureManager;
    }

    @Override
    public List<BinlogEvent> parse(MetaEvent event) {
        TableMapEventData meta = event.getMeta();
        EventData data = event.getData();
        String database = meta.getDatabase();
        String table = meta.getTable();
        long tableId = meta.getTableId();

        Table structure = dataStructureManager.getStructure(database, table, tableId);
        if (Objects.nonNull(structure)) {
            if (data instanceof WriteRowsEventData) {
                WriteRowsEventData eventData = (WriteRowsEventData) data;
                return this.parse(eventData, structure.getDatabase(), structure.getTable(), structure.getColumns());
            }

            if (data instanceof UpdateRowsEventData) {
                UpdateRowsEventData eventData = (UpdateRowsEventData) data;
                return this.parse(eventData, structure.getDatabase(), structure.getTable(), structure.getColumns());
            }

            if (data instanceof DeleteRowsEventData) {
                DeleteRowsEventData eventData = (DeleteRowsEventData) data;
                return this.parse(eventData, structure.getDatabase(), structure.getTable(), structure.getColumns());
            }

        }

        return Collections.emptyList();
    }


    public List<BinlogEvent> parse(WriteRowsEventData writeRowsEventData, String database, String table, List<Column> columns) {

        List<BinlogEvent> result = new ArrayList<>();

        List<Serializable[]> rows = writeRowsEventData.getRows();

        try {
            rows.forEach(row -> {
                Map<String, Object> afterMap = new HashMap<>();
                columns.forEach(col -> afterMap.put(col.getName(), row[col.getPosition() - 1]));

                BinlogEvent event = new BinlogEvent();
                event.setEventType(EventType.INSERT);
                event.setDatabase(database);
                event.setTable(table);
                event.setAfterMap(afterMap);

                result.add(event);
            });
        } catch (Exception e) {
            log.error("解析binlog event 错误：", e);
        }


        return result;
    }


    public List<BinlogEvent> parse(UpdateRowsEventData updateRowsEventData, String database, String table, List<Column> columns) {
        List<BinlogEvent> result = new ArrayList<>();

        List<Map.Entry<Serializable[], Serializable[]>> rows = updateRowsEventData.getRows();
        try {
            rows.forEach(row -> {
                Map<String, Object> afterMap = new HashMap<>();
                Map<String, Object> beforeMap = new HashMap<>();

                Serializable[] beforeRow = row.getKey();
                Serializable[] afterRow = row.getValue();

                columns.forEach(col -> beforeMap.put(col.getName(), beforeRow[col.getPosition() - 1]));

                columns.forEach(col -> afterMap.put(col.getName(), afterRow[col.getPosition() - 1]));

                BinlogEvent event = new BinlogEvent();
                event.setEventType(EventType.UPDATE);
                event.setDatabase(database);
                event.setTable(table);
                event.setBeforeMap(beforeMap);
                event.setAfterMap(afterMap);

                result.add(event);
            });
        } catch (Exception e) {
            log.error("解析binlog event 错误：", e);
        }

        return result;
    }


    public List<BinlogEvent> parse(DeleteRowsEventData deleteRowsEventData, String database, String table, List<Column> columns) {
        List<BinlogEvent> result = new ArrayList<>();

        List<Serializable[]> rows = deleteRowsEventData.getRows();
        try {
            rows.forEach(row -> {
                Map<String, Object> beforeMap = new HashMap<>();
                columns.forEach(col -> beforeMap.put(col.getName(), row[col.getPosition() - 1]));

                BinlogEvent event = new BinlogEvent();
                event.setEventType(EventType.DELETE);
                event.setDatabase(database);
                event.setTable(table);
                event.setBeforeMap(beforeMap);
                result.add(event);
            });
        } catch (Exception e) {
            log.error("解析binlog event 错误：", e);
        }

        return result;
    }


}
