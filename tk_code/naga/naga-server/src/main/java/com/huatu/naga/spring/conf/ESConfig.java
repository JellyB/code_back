package com.huatu.naga.spring.conf;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * @author hanchao
 * @date 2018/1/23 16:11
 */
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.huatu.naga.dao.es")
public class ESConfig {
}
