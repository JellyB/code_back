package com.huatu.tiku.config;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.huatu.common.consts.ApolloConfigConsts;
import com.huatu.tiku.banckend.service.IconService;
import com.huatu.tiku.banckend.service.impl.IconServiceImpl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-11-06 7:53 PM
 **/

@Configuration
@EnableApolloConfig
@EnableConfigurationProperties(Properties.class)
public class IconAutoConfiguration {


    private Properties properties;

    public IconAutoConfiguration(Properties properties) {
        this.properties = properties;
    }

    @Bean
    public IconService iconService() throws IOException {
        return new IconServiceImpl(properties.getIcons());
    }
}
