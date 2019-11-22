package com.huatu.ztk.search.config;

import com.baidu.disconf.client.common.annotations.DisconfFile;
import com.baidu.disconf.client.common.annotations.DisconfFileItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@DisconfFile(filename = "search.properties")
public class SearchConfig {
    private static final Logger logger = LoggerFactory.getLogger(SearchConfig.class);

    private String proxy;


    @DisconfFileItem(name = "proxy", associateField = "proxy")
    public String getProxy() {
        return proxy;
    }

    public void setUrl(String proxy) {
        this.proxy = proxy;
    }
}