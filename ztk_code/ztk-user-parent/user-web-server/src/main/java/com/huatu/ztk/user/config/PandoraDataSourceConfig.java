package com.huatu.ztk.user.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import tk.mybatis.spring.annotation.MapperScan;

import javax.sql.DataSource;

/**
 * Created by lijun on 2018/8/21
 */
@Configuration
@MapperScan(basePackages = "com.huatu.ztk.user.daoPandora",sqlSessionTemplateRef = "pandoraSqlSessionTemplate")
public class PandoraDataSourceConfig {

    @Bean(name = "pandoraSqlSessionFactory")
    public SqlSessionFactory teacherSqlSessionFactory(@Qualifier("pandoraDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        //bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mybatis/mapper/teacher/*.xml"));
        return bean.getObject();
    }

    @Bean(name = "pandoraTransactionManager")
    public DataSourceTransactionManager teacherTransactionManager(@Qualifier("pandoraDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "pandoraSqlSessionTemplate")
    public SqlSessionTemplate teacherSqlSessionTemplate(@Qualifier("pandoraSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
