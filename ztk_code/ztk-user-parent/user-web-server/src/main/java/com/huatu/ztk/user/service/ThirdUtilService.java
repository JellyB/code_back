package com.huatu.ztk.user.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.ztk.user.bean.UcenterBind;
import com.huatu.ztk.user.bean.UcenterMember;
import com.huatu.ztk.user.common.UcenterConfig;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by lijun on 2019/1/9
 */
@Service
public class ThirdUtilService {

    private static final Logger logger = LoggerFactory.getLogger(ThirdUtilService.class);

    /**
     * 默认密码
     */
    private final static String DEFAULT_PASSWORD = "123456";

    @Autowired
    private UserService userService;

    @Autowired
    private UcenterService ucenterService;

    /**
     * 根据用户手机号 获取中心库中的 uid
     *
     * @param phoneList 手机号
     * @return 中心库ID
     */
    public List<HashMap<String, Integer>> getUCenterIdByPhone(List<String> phoneList, String regIp) {
        if (CollectionUtils.isEmpty(phoneList)) {
            return Lists.newArrayList();
        }
        long l = System.currentTimeMillis();
        logger.info("第三方接口，参数列表:{},", phoneList);
        List<UcenterBind> bindByMobileList = ucenterService.findBindByMobileList(phoneList);
        List<HashMap<String, Integer>> mapList = bindByMobileList.stream()
                .map(ucenterBind -> {
                    HashMap<String, Integer> result = Maps.newHashMap();
                    result.put(ucenterBind.getPhone(), ucenterBind.getUserid());
                    return result;
                })
                .collect(Collectors.toList());
        long l1 = System.currentTimeMillis();
        logger.info("第三方接口查询，耗时{},查询结果:{},", l1 - l, mapList);
        if (phoneList.size() == mapList.size()) {
            return mapList;
        }
        //如果出现未注册的用户
        final Set<String> exitedSet = bindByMobileList.stream()
                .map(UcenterBind::getPhone)
                .collect(Collectors.toSet());
        //需要创建的用户
        phoneList.stream()
                .filter(phone -> !exitedSet.contains(phone))
                .forEach(phone -> {
                    UcenterMember ucUser = userService.createUCUser(phone, DEFAULT_PASSWORD, regIp, UcenterConfig.UCENTER_MEMBERS_APPID, false);
                    logger.info("第三方接口新建，{}-新建用户:{}", phone, ucUser);
                    if (null != ucUser) {
                        HashMap<String, Integer> result = Maps.newHashMap();
                        result.put(phone, ucUser.getUid());
                        mapList.add(result);
                    }
                });
        logger.info("第三方接口新建，耗时{},新建结果:{},", System.currentTimeMillis() - l1, mapList);
        return mapList;
    }
}
