package com.huatu.tiku.course.spring.conf.db;

import com.alibaba.druid.pool.DruidDataSource;
import com.huatu.springboot.druid.DruidProperties;
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
 * 引用 backendDataSource 配置文件信息
 * 此处的配置文件 引用 自定义组件-druid 中的配置方式，该配置为主库
 * Created by lijun on 2018/6/19
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(BackendDataSourceProperties.class)
@ConditionalOnClass(DruidDataSource.class)
@MapperScan(basePackages = "com.huatu.tiku.course.dao.manual", sqlSessionTemplateRef = "backendSqlSessionTemplate")
public class BackendDataSourceConfig {

    @Autowired
    private BackendDataSourceProperties druidProperties;

    /**
     * 配置Druid 连接信息
     *
     * @return
     * @throws SQLException
     */
    @Primary
    @Bean(name = "backendDataSource")
    @ConditionalOnProperty(name = "spring.datasource.backend.type", havingValue = "com.alibaba.druid.pool.DruidDataSource")
    public DataSource dataSource() throws SQLException {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(druidProperties.getUrl());
        dataSource.setUsername(druidProperties.getUsername());
        dataSource.setPassword(druidProperties.getPassword());

        if (druidProperties.getInitialSize() != null) {
            dataSource.setInitialSize(druidProperties.getInitialSize());
        }
        if (druidProperties.getMinIdle() != null) {
            dataSource.setMinIdle(druidProperties.getMinIdle());
        }
        if (druidProperties.getMaxActive() != null) {
            dataSource.setMaxActive(druidProperties.getMaxActive());
        }
        if (druidProperties.getMaxWait() != null) {
            dataSource.setMaxWait(druidProperties.getMaxWait());
        }
        if (druidProperties.getTimeBetweenEvictionRunsMillis() != null) {
            dataSource.setTimeBetweenEvictionRunsMillis(druidProperties.getTimeBetweenEvictionRunsMillis());
        }
        if (druidProperties.getMinEvictableIdleTimeMillis() != null) {
            dataSource.setMinEvictableIdleTimeMillis(druidProperties.getMinEvictableIdleTimeMillis());
        }
        if (druidProperties.getValidationQuery() != null) {
            dataSource.setValidationQuery(druidProperties.getValidationQuery());
        }
        dataSource.setPoolPreparedStatements(druidProperties.isPoolPreparedStatements());
        if (druidProperties.isPoolPreparedStatements() && druidProperties.getMaxPoolPreparedStatementPerConnectionSize() != null) {
            dataSource.setMaxPoolPreparedStatementPerConnectionSize(druidProperties.getMaxPoolPreparedStatementPerConnectionSize());
        }
        dataSource.setTestWhileIdle(druidProperties.isTestWhileIdle());
        dataSource.setTestOnBorrow(druidProperties.isTestOnBorrow());
        dataSource.setTestOnReturn(druidProperties.isTestOnReturn());
        if (druidProperties.getMaxPoolPreparedStatementPerConnectionSize() != null) {
            dataSource.setMaxPoolPreparedStatementPerConnectionSize(druidProperties.getMaxPoolPreparedStatementPerConnectionSize());
        }
        if (druidProperties.getFilters() != null) {
            dataSource.setFilters(druidProperties.getFilters());
        }
        if (druidProperties.getConnectionProperties() != null) {
            dataSource.setConnectProperties(druidProperties.getConnectionProperties());
        }
        // druidProperties.setFilters("stat,wall");
        return dataSource;
    }

    @Primary
    @Bean(name = "backendSqlSessionFactory")
    public SqlSessionFactory backendSqlSessionFactory(
            @Qualifier("backendDataSource") DataSource dataSource
    ) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        //bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mybatis/mapper/course/*.xml"));
        return bean.getObject();
    }

    @Primary
    @Bean(name = "backendTransactionManager")
    public DataSourceTransactionManager backendTransactionManager(
            @Qualifier("backendDataSource") DataSource dataSource
    ) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Primary
    @Bean(name = "backendSqlSessionTemplate")
    public SqlSessionTemplate backendSqlSessionTemplate(
            @Qualifier("backendSqlSessionFactory") SqlSessionFactory sqlSessionFactory
    ) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

}
