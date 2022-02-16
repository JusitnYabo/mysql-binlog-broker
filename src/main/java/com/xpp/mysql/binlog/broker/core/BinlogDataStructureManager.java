package com.xpp.mysql.binlog.broker.core;


import com.xpp.mysql.binlog.broker.model.Table;

/**
 * binlog数据结构管理器
 *
 * @author huyapeng
 * @date 2022/2/9
 * Email: yapeng@jspwork.cn
 */
public interface BinlogDataStructureManager {


    /**
     * 获取表结构信息
     *
     * @param table 表名
     * @return 表结构详细
     */
    Table getStructure(String database, String table, long tableId);


}
