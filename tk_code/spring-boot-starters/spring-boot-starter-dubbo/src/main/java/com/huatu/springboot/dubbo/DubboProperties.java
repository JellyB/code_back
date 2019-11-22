package com.huatu.springboot.dubbo;

import com.alibaba.dubbo.config.*;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 修改原本简单属性威嵌套bean全属性
 * dubbo properties
 *
 * @author xionghui
 * @email xionghui.xh@alibaba-inc.com
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "spring.dubbo")
public class DubboProperties {
    /**
     *
     */
    private ApplicationConfig application = new ApplicationConfig();
    /**
     *
     */
    private RegistryConfig registry = new RegistryConfig();
    /**
     *
     */
    private ProtocolConfig protocol = new ProtocolConfig();
    /**
     *
     */
    private ProviderConfig provider = new ProviderConfig();

    /**
     *
     */
    private ConsumerConfig consumer = new ConsumerConfig();


}
