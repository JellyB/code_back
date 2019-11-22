package com.huatu.springboot.dubbo;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.huatu.springboot.dubbo.endpoint.DubboEndpoint;
import com.huatu.springboot.dubbo.endpoint.DubboOperationEndpoint;
import com.huatu.springboot.dubbo.metrics.DubboMetrics;
import com.huatu.springboot.dubbo.health.DubboHealthIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Dubbo common configuration
 *
 * @author xionghui
 * @email xionghui.xh@alibaba-inc.com
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(DubboProperties.class)
public class DubboAutoConfiguration {
    @Autowired
    private DubboProperties properties;

    @Bean
    @ConditionalOnMissingBean
    public ApplicationConfig dubboApplicationConfig() {
        return properties.getApplication();
    }

    @Bean
    @ConditionalOnMissingBean
    public ProtocolConfig dubboProtocolConfig() {
        return properties.getProtocol();
    }

    @Bean
    @ConditionalOnMissingBean
    public RegistryConfig dubboRegistryConfig() {
        return properties.getRegistry();
    }

    @Bean
    public DubboHealthIndicator dubboHealthIndicator() {
        return new DubboHealthIndicator();
    }

    @Bean
    public DubboEndpoint dubboEndpoint() {
        return new DubboEndpoint();
    }

    @Bean
    public DubboMetrics dubboConsumerMetrics() {
        return new DubboMetrics();
    }


    @Bean
    public DubboOperationEndpoint dubboOperationEndpoint() {
        return new DubboOperationEndpoint();
    }

}
