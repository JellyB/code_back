package com.huatu.tiku.springboot.basic.support;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author hanchao
 * @date 2017/10/6 9:39
 */
@ConfigurationProperties(prefix = "tiku.basic")
@Data
public class BasicProperties {
    private String subjects;
    private String rewardActions;
}
