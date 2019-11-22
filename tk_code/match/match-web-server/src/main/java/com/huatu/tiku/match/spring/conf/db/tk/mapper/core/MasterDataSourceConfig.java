package com.huatu.tiku.match.spring.conf.db.tk.mapper.core;

import com.alibaba.druid.pool.DruidDataSource;
import com.huatu.tiku.match.spring.conf.db.tk.mapper.config.MasterDataSourceProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import tk.mybatis.spring.annotation.MapperScan;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * 引用 masterDataSource 配置文件信息
 * 此处的配置文件 引用 自定义组件-druid 中的配置方式，该配置为主库
 * Created by lijun on 2018/6/19
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(MasterDataSourceProperties.class)
@ConditionalOnClass({DruidDataSource.class, MasterDataSourceProperties.class})
@MapperScan(basePackages = "com.huatu.tiku.match.dao.manual", sqlSessionTemplateRef = "masterSqlSessionTemplate")
public class MasterDataSourceConfig {

    @Autowired
    private MasterDataSourceProperties druidProperties;

    /**
     * 配置Druid 连接信息
     */
    @Primary
    @Bean(name = "masterDataSource")
    public DataSource dataSource() throws SQLException {
        DataSource dataSource = DruidDataSourceFactory.createDataSource(druidProperties);
        druidProperties.setFilters("stat,wall");
        return dataSource;
    }

    @Primary
    @Bean(name = "masterSqlSessionFactory")
    public SqlSessionFactory masterSqlSessionFactory(@Qualifier("masterDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        //bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mybatis/mapper/course/*.xml"));
        return bean.getObject();
    }

    @Primary
    @Bean(name = "masterTransactionManager")
    public DataSourceTransactionManager masterTransactionManager(@Qualifier("masterDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Primary
    @Bean(name = "masterSqlSessionTemplate")
    public SqlSessionTemplate masterSqlSessionTemplate(@Qualifier("masterSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

}
