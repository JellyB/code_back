package com.huatu.ztk.user.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 工具类 - 抽离 原有的业务代码
 * Created by lijun on 2018/10/9
 */
@Component
public class UserSystemControllerUtil {

    private static final Logger logger = LoggerFactory.getLogger(UserSystemControllerUtil.class);


    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 用以收集用户的设备信息
     *
     * @param device    设备类型
     * @param system    操作系统版本
     * @param telephone 收集型号
     */
    @Async
    public void storeDeviceInfo(String device, String system, String telephone) {
        //设备 转换成 设备-操作系统
        device = device + "-" + system;
        if (isLegal(device, telephone)) {
            HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
            String telephoneList = hashOperations.get(getCacheKey(), device);
            if (StringUtils.isBlank(telephoneList)) {
                telephoneList = telephone;
            } else {
                List<String> list = new ArrayList<>();
                list.addAll(Arrays.asList(telephoneList.split(",")));
                list.add(telephone);
                telephoneList = StringUtils.join(list, ",");
            }
            if (StringUtils.isNotBlank(telephoneList)) {
                hashOperations.put(getCacheKey(), device, telephoneList);
            }
        }
    }

    /**
     * 验证参数是否合法
     */
    public boolean isLegal(String device, String telephone) {
        if (StringUtils.isBlank(device) || StringUtils.isBlank(telephone)) {
            return false;
        }
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        String telephoneList = hashOperations.get(getCacheKey(), device);
        if (StringUtils.isNotBlank(telephoneList)) {
            for (String exitPhone : telephoneList.split(",")) {
                if (exitPhone.equals(telephone)) {
                    //该号码已经存在
                    return false;
                }
            }
        }
        //当前未收集到 手机号、当前的手机号数量 小于3
        return StringUtils.isBlank(telephoneList) || telephoneList.split(",").length < 3;

    }

    private static String getCacheKey() {
        return "_info:user:device:collect";
    }

}
