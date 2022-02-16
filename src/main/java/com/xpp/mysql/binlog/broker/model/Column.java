package com.xpp.mysql.binlog.broker.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 列数据结构
 *
 * @author huyapeng
 * @date 2022/2/9
 * Email: yapeng@jspwork.cn
 */
@Data
public class Column implements Serializable {
    private String name;
    private int type;
    private int position;
}
