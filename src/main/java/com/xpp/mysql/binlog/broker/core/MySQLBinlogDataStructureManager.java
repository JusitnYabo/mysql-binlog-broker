package com.xpp.mysql.binlog.broker.core;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.xpp.mysql.binlog.broker.model.Column;
import com.xpp.mysql.binlog.broker.model.Table;
import com.xpp.mysql.binlog.broker.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author huyapeng
 * @date 2022/2/9
 * Email: yapeng@jspwork.cn
 */
@Slf4j
public class MySQLBinlogDataStructureManager implements BinlogDataStructureManager {


    private static final String queryColumnsSql = "SELECT COLUMN_NAME, ORDINAL_POSITION FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = '%s' AND TABLE_NAME = '%s' ";

    private final JdbcTemplate jdbcTemplate;

    private final StringRedisTemplate stringRedisTemplate;

    public MySQLBinlogDataStructureManager(DataSource dataSource, StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private final Cache<String, Table> localCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();


    /**
     * 设置较长缓存时间，较短刷新时间，防止宕机时间过长
     *
     * @param table 表名
     * @return 表结构
     */
    @Override
    public Table getStructure(String database, String table, long tableId) {
        return localCache.get(database + "_" + table + "_" + tableId, key -> this.loadFromRemoteCache(database, table, tableId));
    }


    /**
     * 从远程缓存中加载
     *
     * @return 列数据结构
     */
    private Table loadFromRemoteCache(String database, String table, long tableId) {
        String redisKey = "binlog::data::structure::" + database + "_" + table + "_" + tableId;
        String value = stringRedisTemplate.opsForValue().get(redisKey);
        return Optional.ofNullable(JsonUtils.fromJson(value, Table.class)).orElseGet(() -> {
            Table result = this.loadFromDB(database, table, tableId);
            stringRedisTemplate.opsForValue().set(redisKey, JsonUtils.toJson(result), 3, TimeUnit.DAYS);
            return result;
        });
    }


    /**
     * 数据库中读取表结构信息
     *
     * @param table 表名
     * @return 列结构
     */
    private Table loadFromDB(String database, String table, long tableId) {
        List<Column> columns = new ArrayList<>();

        jdbcTemplate.query(String.format(queryColumnsSql, database, table), resultSet -> {
            String columnName = resultSet.getString("COLUMN_NAME");
            int ordinalPosition = resultSet.getInt("ORDINAL_POSITION");
            Column column = new Column();
            column.setName(columnName);
            column.setPosition(ordinalPosition);

            columns.add(column);
        });

        Table result = new Table();
        result.setTable(table);
        result.setDatabase(database);
        result.setTableId(tableId);
        result.setColumns(columns);
        return result;
    }

}
