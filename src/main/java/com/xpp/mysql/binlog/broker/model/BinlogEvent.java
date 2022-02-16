package com.xpp.mysql.binlog.broker.model;

import com.xpp.mysql.binlog.broker.enums.EventType;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Data
public class BinlogEvent {

    EventType eventType;

    private String database;

    private String table;

    Map<String, Object> afterMap;

    Map<String, Object> beforeMap;

    public Map<String, Object> getValueMap() {
        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("eventType", eventType);
        if (Objects.equals(eventType, EventType.DELETE)) {
            if (Objects.nonNull(beforeMap))
                beforeMap.forEach((k, v) -> valueMap.put(underlineToCamel(k), v));
        } else {
            if (Objects.nonNull(afterMap))
                afterMap.forEach((k, v) -> valueMap.put(underlineToCamel(k), v));

        }
        return valueMap;
    }

    /**
     * 下划线，转驼峰命名
     *
     * @param columnName 列名
     * @return 驼峰命名
     */
    private String underlineToCamel(String columnName) {

        // 快速检查
        if (columnName == null || columnName.isEmpty()) {
            // 没必要转换
            return "";
        } else if (!columnName.contains("_")) {
            // 不含下划线，仅将首字母小写
            return columnName.substring(0, 1).toLowerCase() + columnName.substring(1);
        }

        StringBuilder result = new StringBuilder();
        // 用下划线将原始字符串分割
        String[] camels = columnName.split("_");
        for (String camel : camels) {
            // 跳过原始字符串中开头、结尾的下换线或双重下划线
            if (camel.isEmpty()) {
                continue;
            }
            // 处理真正的驼峰片段
            if (result.length() == 0) {
                // 第一个驼峰片段，全部字母都小写
                result.append(camel.toLowerCase());
            } else {
                // 其他的驼峰片段，首字母大写
                result.append(camel.substring(0, 1).toUpperCase());
                result.append(camel.substring(1).toLowerCase());
            }
        }
        return result.toString();
    }

}
