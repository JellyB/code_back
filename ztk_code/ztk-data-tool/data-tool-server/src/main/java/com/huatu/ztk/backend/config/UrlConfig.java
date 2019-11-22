package com.huatu.ztk.backend.config;

import com.baidu.disconf.client.common.annotations.DisconfItem;
import org.springframework.stereotype.Component;

/**
 * @author hanchao
 * @date 2018/1/4 16:37
 */
@Component
public class UrlConfig {
    private String essayUrl;

    @DisconfItem(key="essayUrl")
    public String getEssayUrl() {
        return essayUrl;
    }

    public void setEssayUrl(String essayUrl) {
        this.essayUrl = essayUrl;
    }
}
