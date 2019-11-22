package com.huatu.tiku.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-11-06 7:54 PM
 **/

@Data
@ConfigurationProperties(prefix = "default")
public class Properties {
    private String icons;
}
