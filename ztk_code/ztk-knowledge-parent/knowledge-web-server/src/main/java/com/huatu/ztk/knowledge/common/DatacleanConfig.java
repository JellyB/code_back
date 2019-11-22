package com.huatu.ztk.knowledge.common;

import com.baidu.disconf.client.common.annotations.DisconfFile;
import com.baidu.disconf.client.common.annotations.DisconfFileItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author hanchao
 * @date 2017/10/4 9:44
 */
@Component
@DisconfFile(filename = "dataclean.properties")
public class DatacleanConfig {
    private static final Logger log = LoggerFactory.getLogger(DatacleanConfig.class);
    private int fetchSize = 5000;
    private int breaker; // 0 为关闭，1为打开

    private int serviceFlag;  //1为v1，2为v2

    @DisconfFileItem(name = "serviceFlag", associateField = "serviceFlag")
    public int getServiceFlag() {
        return serviceFlag;
    }

    public void setServiceFlag(int serviceFlag) {
        this.serviceFlag = serviceFlag;
    }

    @DisconfFileItem(name = "fetchSize", associateField = "fetchSize")
    public int getFetchSize() {
        return fetchSize;
    }

    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }

    @DisconfFileItem(name = "breaker", associateField = "breaker")
    public int getBreaker() {
        return breaker;
    }

    public void setBreaker(int breaker) {
        this.breaker = breaker;
    }
}
