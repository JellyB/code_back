package com.huatu.tiku.interview.spring.conf.db;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import static com.huatu.common.consts.ApolloConfigConsts.NAMESPACE_TIKU_DB;

/**
 * @author hanchao
 * @date 2017/11/22 16:49
 */
@Configuration
@EnableApolloConfig(NAMESPACE_TIKU_DB)
@EnableTransactionManagement(proxyTargetClass=true)
@EnableJpaRepositories(value = "com.huatu.tiku.interview.repository",repositoryImplementationPostfix = "Impl")//jpa包配置
@EntityScan("com.huatu.tiku.interview.entity.po")//jpa实体类包配置
public class JpaConfig {
}
