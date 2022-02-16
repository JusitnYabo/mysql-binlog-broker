package com.xpp.mysql.binlog.broker.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 表结构
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Table implements Serializable {
    /**
     * 所属数据库
     */
    private String database;
    /**
     * 表名
     */
    private String table;
    /**
     * 当前表id(mysql表结构变动tableId发生改变)
     */
    private long tableId;

    private List<Column> columns;

}
