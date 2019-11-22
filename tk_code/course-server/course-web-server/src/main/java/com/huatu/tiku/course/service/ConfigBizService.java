package com.huatu.tiku.course.service;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;

/**
 * 系统配置
 * @author hanchao
 * @date 2017/9/22 12:40
 */
@Service
@Slf4j
public class ConfigBizService {
    private Map<String,String> ADDRESS_CONFIG = Maps.newHashMap();
    private static final String ADDRESS_CONFIG_FILE = "address.xml";
    @Value("${server.context-path:/c}")
    private String contextPath;

    @PostConstruct
    public void init(){
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        Resource addressResource = resourcePatternResolver.getResource("classpath:static/settings/" + ADDRESS_CONFIG_FILE);
        try {
            String etag =  DigestUtils.md5DigestAsHex(addressResource.getInputStream());
            ADDRESS_CONFIG.put("etag",etag);
        } catch (IOException e) {
            ADDRESS_CONFIG.put("etag","");
            log.error("get address etag error,will use empty",e);
        }
        ADDRESS_CONFIG.put("links",contextPath+"/static/settings/"+ADDRESS_CONFIG_FILE);
    }

    public Map getConfig(){
        return ADDRESS_CONFIG;
    }

}
