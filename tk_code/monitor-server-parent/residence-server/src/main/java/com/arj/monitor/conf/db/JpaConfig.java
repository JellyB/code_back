package com.arj.monitor.conf.db;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;


/**
 * @author zhouwei
 * @Description: jpa配置
 * @create 2018-10-15 上午11:02
 **/
@Configuration
@EnableTransactionManagement(proxyTargetClass=true)
@EnableJpaRepositories(value = "com.arj.monitor.repository",repositoryImplementationPostfix = "Impl")//jpa包配置
@EntityScan("com.arj.monitor.entity")//jpa实体类包配置
public class JpaConfig {
}
