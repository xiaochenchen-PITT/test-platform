package com.cxc.test.platform.infra.utils;

import com.cxc.test.platform.infra.config.DatabaseConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.http.util.Asserts;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@Slf4j
public class JdbcUtils {

    public static DataSource intiDataSource(DatabaseConfig databaseConfig) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(databaseConfig.getDriverClassName());
        dataSource.setUrl(databaseConfig.getUrl());
        dataSource.setUsername(databaseConfig.getName());
        dataSource.setPassword(databaseConfig.getPwd());

        return dataSource;
    }

    public static List<Map<String, Object>> queryResult(DataSource dataSource, String sql) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.queryForList(sql);
    }

    /**
     * sql只能获取一行数据，即sql能唯一定义一个元素
     */
    public static Object getSingleValueBySql(DataSource dataSource, String sql, String fieldName) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
        // = 0
        if (CollectionUtils.isEmpty(result)) {
            return null;
        }

        Asserts.check(result.size() == 1, "result has more than 1 size, sql: %s", sql);
        return result.get(0).get(fieldName);
    }
}
