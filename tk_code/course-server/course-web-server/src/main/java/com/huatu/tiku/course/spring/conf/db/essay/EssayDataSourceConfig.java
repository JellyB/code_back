package com.huatu.tiku.course.spring.conf.db.essay;

import com.alibaba.druid.pool.DruidDataSource;
import com.huatu.tiku.course.spring.conf.db.BackendDataSourceProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import tk.mybatis.spring.annotation.MapperScan;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * 引用 essayDataSource 配置文件信息
 * 此处的配置文件 引用 自定义组件-druid 中的配置方式，该配置为主库
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(EssayDataSourceProperties.class)
@ConditionalOnClass(DruidDataSource.class)
@MapperScan(basePackages = "com.huatu.tiku.course.dao.essay", sqlSessionTemplateRef = "essaySqlSessionTemplate")
public class EssayDataSourceConfig {

    @Autowired
    private EssayDataSourceProperties essayDataSourceProperties;

    /**
     * 配置Druid 连接信息
     *
     * @return
     * @throws SQLException
     */
    @Bean(name = "essayDataSource")
    @ConditionalOnProperty(name = "spring.datasource.backend.type", havingValue = "com.alibaba.druid.pool.DruidDataSource")
    public DataSource dataSource() throws SQLException {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(essayDataSourceProperties.getUrl());
        dataSource.setUsername(essayDataSourceProperties.getUsername());
        dataSource.setPassword(essayDataSourceProperties.getPassword());

        if (essayDataSourceProperties.getInitialSize() != null) {
            dataSource.setInitialSize(essayDataSourceProperties.getInitialSize());
        }
        if (essayDataSourceProperties.getMinIdle() != null) {
            dataSource.setMinIdle(essayDataSourceProperties.getMinIdle());
        }
        if (essayDataSourceProperties.getMaxActive() != null) {
            dataSource.setMaxActive(essayDataSourceProperties.getMaxActive());
        }
        if (essayDataSourceProperties.getMaxWait() != null) {
            dataSource.setMaxWait(essayDataSourceProperties.getMaxWait());
        }
        if (essayDataSourceProperties.getTimeBetweenEvictionRunsMillis() != null) {
            dataSource.setTimeBetweenEvictionRunsMillis(essayDataSourceProperties.getTimeBetweenEvictionRunsMillis());
        }
        if (essayDataSourceProperties.getMinEvictableIdleTimeMillis() != null) {
            dataSource.setMinEvictableIdleTimeMillis(essayDataSourceProperties.getMinEvictableIdleTimeMillis());
        }
        if (essayDataSourceProperties.getValidationQuery() != null) {
            dataSource.setValidationQuery(essayDataSourceProperties.getValidationQuery());
        }
        dataSource.setPoolPreparedStatements(essayDataSourceProperties.isPoolPreparedStatements());
        if (essayDataSourceProperties.isPoolPreparedStatements() && essayDataSourceProperties.getMaxPoolPreparedStatementPerConnectionSize() != null) {
            dataSource.setMaxPoolPreparedStatementPerConnectionSize(essayDataSourceProperties.getMaxPoolPreparedStatementPerConnectionSize());
        }
        dataSource.setTestWhileIdle(essayDataSourceProperties.isTestWhileIdle());
        dataSource.setTestOnBorrow(essayDataSourceProperties.isTestOnBorrow());
        dataSource.setTestOnReturn(essayDataSourceProperties.isTestOnReturn());
        if (essayDataSourceProperties.getMaxPoolPreparedStatementPerConnectionSize() != null) {
            dataSource.setMaxPoolPreparedStatementPerConnectionSize(essayDataSourceProperties.getMaxPoolPreparedStatementPerConnectionSize());
        }
        if (essayDataSourceProperties.getFilters() != null) {
            dataSource.setFilters(essayDataSourceProperties.getFilters());
        }
        if (essayDataSourceProperties.getConnectionProperties() != null) {
            dataSource.setConnectProperties(essayDataSourceProperties.getConnectionProperties());
        }
        return dataSource;
    }

    @Bean(name = "essaySqlSessionFactory")
    public SqlSessionFactory essaySqlSessionFactory(
            @Qualifier("essayDataSource") DataSource dataSource
    ) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        return bean.getObject();
    }

    @Bean(name = "essayTransactionManager")
    public DataSourceTransactionManager essayTransactionManager(
            @Qualifier("essayDataSource") DataSource dataSource
    ) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "essaySqlSessionTemplate")
    public SqlSessionTemplate essaySqlSessionTemplate(
            @Qualifier("essaySqlSessionFactory") SqlSessionFactory sqlSessionFactory
    ) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

}
