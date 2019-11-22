package com.huatu.ztk.user.service;

import com.baidu.disconf.client.common.annotations.DisconfFileItem;
import com.baidu.disconf.client.common.annotations.DisconfItem;
import org.springframework.stereotype.Component;

/**
 * @author hanchao
 * @date 2018/1/4 16:37
 */
@Component
public class UserServerConfig {
    private String seckillUrl;

    private String environment;

    private Integer registerClassId;

    private String 	registerCreateOrderUrl;

    private String defaultIcons;
    
    private String wechatCheckStatus;

    @DisconfItem(key = "wechat.check.status")
    public String getWechatCheckStatus() {
		return wechatCheckStatus;
	}

	public void setWechatCheckStatus(String wechatCheckStatus) {
		this.wechatCheckStatus = wechatCheckStatus;
	}

	@DisconfItem(key = "php.base.url")
    public String getPhpBaseUrl() {
        return phpBaseUrl;
    }

    public void setPhpBaseUrl(String phpBaseUrl) {
        this.phpBaseUrl = phpBaseUrl;
    }

    private String phpBaseUrl;


    @DisconfItem(key = "seckillUrl")
    public String getSeckillUrl() {
        return seckillUrl;
    }

    public void setSeckillUrl(String seckillUrl) {
        this.seckillUrl = seckillUrl;
    }

    @DisconfItem(key = "environment")
    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    private String updateWhiteList;

    @DisconfItem(key = "app.version.update.white.list")
    public String getUpdateWhiteList() {
        return updateWhiteList;
    }

    public void setUpdateWhiteList(String updateWhiteList) {
        this.updateWhiteList = updateWhiteList;
    }

    @DisconfItem(key = "user.register.classId")
    public Integer getRegisterClassId() {
        return registerClassId;
    }

    public void setRegisterClassId(Integer registerClassId) {
        this.registerClassId = registerClassId;
    }

    @DisconfItem(key = "user.register.createOrder.url")
    public String getRegisterCreateOrderUrl() {
        return registerCreateOrderUrl;
    }

    public void setRegisterCreateOrderUrl(String registerCreateOrderUrl) {
        this.registerCreateOrderUrl = registerCreateOrderUrl;
    }

    @DisconfItem(key = "subject.default.icons")
    public String getDefaultIcons() {
        return defaultIcons;
    }

    public void setDefaultIcons(String defaultIcons) {
        this.defaultIcons = defaultIcons;
    }
}
