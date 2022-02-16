package com.xpp.mysql.binlog.broker.core;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@SpringBootTest
class KafkaBinlogEventListenerTest {


    private static final String sqlTemplate = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA ";

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Test
    public void showDatabases() {
        List<String> schemas = new ArrayList<>();

        jdbcTemplate.query(sqlTemplate, resultSet -> {
            String schema = resultSet.getString("SCHEMA_NAME");
            schemas.add(schema);
        });
        schemas.forEach(System.err::println);


    }


}