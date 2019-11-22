package com.huatu.ztk.knowledge.common.analysis.config;

import com.baidu.disconf.client.common.annotations.DisconfItem;
import org.springframework.stereotype.Component;

/**
 * @author zhengyi
 * @date 2019-01-14 16:19
 **/
@Component
public class SaProperties {
    private String saUrl;

    @DisconfItem(key = "saUrl")
    public String getSaUrl() {
        return saUrl;
    }

    public void setSaUrl(String saUrl) {
        this.saUrl = saUrl;
    }


    private String payByCoinUrl;

    @DisconfItem(key = "payByCoinUrl")
    public String getPayByCoinUrl() {
        return payByCoinUrl;
    }

    public void setPayByCoinUrl(String payByCoinUrl) {
        this.payByCoinUrl = payByCoinUrl;
    }
}