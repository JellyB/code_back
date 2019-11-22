package com.huatu.springboot.druid;

import com.alibaba.druid.pool.DruidDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * @author hanchao
 * @date 2018/4/17 16:26
 */
public class DruidDataSourceFactory {

    public static DataSource createDataSource(DruidDataSourceProperties druidProperties) throws SQLException {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(druidProperties.getUrl());
        dataSource.setUsername(druidProperties.getUsername());
        dataSource.setPassword(druidProperties.getPassword());
        if(druidProperties.getInitialSize()!=null){
            dataSource.setInitialSize(druidProperties.getInitialSize());
        }
        if(druidProperties.getMinIdle()!=null){
            dataSource.setMinIdle(druidProperties.getMinIdle());
        }
        if(druidProperties.getMaxActive()!=null){
            dataSource.setMaxActive(druidProperties.getMaxActive());
        }
        if(druidProperties.getMaxWait()!=null){
            dataSource.setMaxWait(druidProperties.getMaxWait());
        }
        if(druidProperties.getTimeBetweenEvictionRunsMillis()!=null){
            dataSource.setTimeBetweenEvictionRunsMillis(druidProperties.getTimeBetweenEvictionRunsMillis());
        }
        if(druidProperties.getMinEvictableIdleTimeMillis()!=null){
            dataSource.setMinEvictableIdleTimeMillis(druidProperties.getMinEvictableIdleTimeMillis());
        }
        if(druidProperties.getValidationQuery() != null){
            dataSource.setValidationQuery(druidProperties.getValidationQuery());
        }
        dataSource.setPoolPreparedStatements(druidProperties.isPoolPreparedStatements());
        if(druidProperties.isPoolPreparedStatements() && druidProperties.getMaxPoolPreparedStatementPerConnectionSize()!=null){
            dataSource.setMaxPoolPreparedStatementPerConnectionSize(druidProperties.getMaxPoolPreparedStatementPerConnectionSize());
        }
        dataSource.setTestWhileIdle(druidProperties.isTestWhileIdle());
        dataSource.setTestOnBorrow(druidProperties.isTestOnBorrow());
        dataSource.setTestOnReturn(druidProperties.isTestOnReturn());
        if(druidProperties.getMaxPoolPreparedStatementPerConnectionSize()!=null){
            dataSource.setMaxPoolPreparedStatementPerConnectionSize(druidProperties.getMaxPoolPreparedStatementPerConnectionSize());
        }
        if(druidProperties.getFilters()!=null){
            dataSource.setFilters(druidProperties.getFilters());
        }
        if(druidProperties.getConnectionProperties()!=null){
            dataSource.setConnectProperties(druidProperties.getConnectionProperties());
        }
        return dataSource;
    }
}
