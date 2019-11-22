package com.huatu.ztk.scm.base;

import javax.annotation.Resource;

import org.springframework.jdbc.core.JdbcTemplate;

public abstract class BaseDao {
    @Resource
    private JdbcTemplate jdbcTemplate;

    protected JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }
}
