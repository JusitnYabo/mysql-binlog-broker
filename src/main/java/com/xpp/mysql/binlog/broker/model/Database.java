package com.xpp.mysql.binlog.broker.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 * @author huyapeng
 * @date 2022/2/11
 * Email: yapeng@jspwork.cn
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Database {

    /**
     * 数据库 host address
     */
    private String host;
    /**
     * 数据库 port
     */
    private int port;
    /**
     * 数据库 username
     */
    private String username;
    /**
     * 数据库 password
     */
    private String password;
    /**
     * 数据库名称
     */
    private String database;
    /**
     * 是否启用
     */
    private boolean enable;

    private List<Table> tables;

}
