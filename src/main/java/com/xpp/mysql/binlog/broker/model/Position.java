package com.xpp.mysql.binlog.broker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Position implements Serializable {

    /**
     * 数据库实例名称
     */
    private String name;
    /**
     * 日志文件名
     */
    private String file;
    /**
     * 当前位置
     */
    private long offset;


}
