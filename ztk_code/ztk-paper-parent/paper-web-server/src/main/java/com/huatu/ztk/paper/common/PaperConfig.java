package com.huatu.ztk.paper.common;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by shaojieyue
 * Created time 2016-05-05 20:37
 */

@Component("paper-config")
public class PaperConfig {
    @Value("${id.generator.zk.address}")
    public String ID_GENERATOR_ZK_ADDRESS;
}
