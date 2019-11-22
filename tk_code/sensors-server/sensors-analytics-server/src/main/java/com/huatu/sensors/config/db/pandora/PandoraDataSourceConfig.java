package com.huatu.sensors.config.db.pandora;

import java.sql.SQLException;

import javax.sql.DataSource;

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

import com.alibaba.druid.pool.DruidDataSource;

import lombok.extern.slf4j.Slf4j;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * 引用 pandoraDataSource 配置文件信息 此处的配置文件 引用 自定义组件-druid 中的配置方式，该配置为主库
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(PandoraDataSourceProperties.class)
@ConditionalOnClass(DruidDataSource.class)
@MapperScan(basePackages = "com.huatu.sensors.dao.pandora", sqlSessionTemplateRef = "pandoraSqlSessionTemplate")
public class PandoraDataSourceConfig {

	@Autowired
	private PandoraDataSourceProperties pandoraDataSourceProperties;

	/**
	 * 配置Druid 连接信息
	 *
	 * @return
	 * @throws SQLException
	 */
	@Primary
	@Bean(name = "pandoraDataSource")
	@ConditionalOnProperty(name = "spring.datasource.essay.type", havingValue = "com.alibaba.druid.pool.DruidDataSource")
	public DataSource dataSource() throws SQLException {
		DruidDataSource dataSource = new DruidDataSource();
		dataSource.setUrl(pandoraDataSourceProperties.getUrl());
		dataSource.setUsername(pandoraDataSourceProperties.getUsername());
		dataSource.setPassword(pandoraDataSourceProperties.getPassword());

		if (pandoraDataSourceProperties.getInitialSize() != null) {
			dataSource.setInitialSize(pandoraDataSourceProperties.getInitialSize());
		}
		if (pandoraDataSourceProperties.getMinIdle() != null) {
			dataSource.setMinIdle(pandoraDataSourceProperties.getMinIdle());
		}
		if (pandoraDataSourceProperties.getMaxActive() != null) {
			dataSource.setMaxActive(pandoraDataSourceProperties.getMaxActive());
		}
		if (pandoraDataSourceProperties.getMaxWait() != null) {
			dataSource.setMaxWait(pandoraDataSourceProperties.getMaxWait());
		}
		if (pandoraDataSourceProperties.getTimeBetweenEvictionRunsMillis() != null) {
			dataSource.setTimeBetweenEvictionRunsMillis(pandoraDataSourceProperties.getTimeBetweenEvictionRunsMillis());
		}
		if (pandoraDataSourceProperties.getMinEvictableIdleTimeMillis() != null) {
			dataSource.setMinEvictableIdleTimeMillis(pandoraDataSourceProperties.getMinEvictableIdleTimeMillis());
		}
		if (pandoraDataSourceProperties.getValidationQuery() != null) {
			dataSource.setValidationQuery(pandoraDataSourceProperties.getValidationQuery());
		}
		dataSource.setPoolPreparedStatements(pandoraDataSourceProperties.isPoolPreparedStatements());
		if (pandoraDataSourceProperties.isPoolPreparedStatements()
				&& pandoraDataSourceProperties.getMaxPoolPreparedStatementPerConnectionSize() != null) {
			dataSource.setMaxPoolPreparedStatementPerConnectionSize(
					pandoraDataSourceProperties.getMaxPoolPreparedStatementPerConnectionSize());
		}
		dataSource.setTestWhileIdle(pandoraDataSourceProperties.isTestWhileIdle());
		dataSource.setTestOnBorrow(pandoraDataSourceProperties.isTestOnBorrow());
		dataSource.setTestOnReturn(pandoraDataSourceProperties.isTestOnReturn());
		if (pandoraDataSourceProperties.getMaxPoolPreparedStatementPerConnectionSize() != null) {
			dataSource.setMaxPoolPreparedStatementPerConnectionSize(
					pandoraDataSourceProperties.getMaxPoolPreparedStatementPerConnectionSize());
		}
		if (pandoraDataSourceProperties.getFilters() != null) {
			dataSource.setFilters(pandoraDataSourceProperties.getFilters());
		}
		if (pandoraDataSourceProperties.getConnectionProperties() != null) {
			dataSource.setConnectProperties(pandoraDataSourceProperties.getConnectionProperties());
		}
		return dataSource;
	}

	@Primary
	@Bean(name = "pandoraSqlSessionFactory")
	public SqlSessionFactory pandoraSqlSessionFactory(@Qualifier("pandoraDataSource") DataSource dataSource)
			throws Exception {
		SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
		bean.setDataSource(dataSource);
		return bean.getObject();
	}

	@Primary
	@Bean(name = "pandoraTransactionManager")
	public DataSourceTransactionManager pandoraTransactionManager(
			@Qualifier("pandoraDataSource") DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}

	@Primary
	@Bean(name = "pandoraSqlSessionTemplate")
	public SqlSessionTemplate pandoraSqlSessionTemplate(
			@Qualifier("pandoraSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}

}
