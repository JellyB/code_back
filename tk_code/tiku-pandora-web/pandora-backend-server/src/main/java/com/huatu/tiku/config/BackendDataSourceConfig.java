package com.huatu.tiku.config;

import com.alibaba.druid.pool.DruidDataSource;
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
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import tk.mybatis.spring.annotation.MapperScan;

import javax.sql.DataSource;
import java.sql.SQLException;

@Slf4j
@Configuration
@EnableConfigurationProperties(BackendDataSourceProperties.class)
@ConditionalOnClass(DruidDataSource.class)
@MapperScan(basePackages = "com.huatu.tiku.banckend.dao.manual", sqlSessionTemplateRef = "backendSqlSessionTemplate")
public class BackendDataSourceConfig {

    @Autowired
    BackendDataSourceProperties druidProperties;

    /**
     * 配置Druid 连接信息
     *
     * @return
     * @throws SQLException
     */
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
//        if (druidProperties.getFilters() != null) {
//            dataSource.setFilters(druidProperties.getFilters());
//        }
        if (druidProperties.getConnectionProperties() != null) {
            dataSource.setConnectProperties(druidProperties.getConnectionProperties());
        }
        druidProperties.setFilters("stat,wall");
        return dataSource;
    }

    @Bean(name = "backendSqlSessionFactory")
    public SqlSessionFactory backendSqlSessionFactory(@Qualifier("backendDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);

        //bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mybatis/mapper/backend/*.xml"));
        return bean.getObject();
    }

    @Bean(name = "backendSqlSessionTemplate")
    public SqlSessionTemplate backendSqlSessionTemplate(@Qualifier("backendSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean(name = "backendTransactionManager")
    public DataSourceTransactionManager backendTransactionManager(@Qualifier("backendDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }


}
