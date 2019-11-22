package com.huatu.hadoop.conf.mysql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class MysqlConfiguration {

    @Autowired
    private JdbcTemplate jdbcTemplate;


}
