package com.huatu.ztk.user.controller;

import com.google.common.collect.Lists;
import com.huatu.ztk.user.common.RegexConfig;
import com.huatu.ztk.user.service.ThirdUtilService;
import com.huatu.ztk.user.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by lijun on 2019/1/9
 */
@CrossOrigin
@RestController
@RequestMapping(value = "/v1/thirdUtil", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class ThirdUtilController {

    private static final int TELEPHONE_MAX_SIZE = 20;

    @Autowired
    private UserService userService;

    @Autowired
    private ThirdUtilService thirdUtilService;

    /**
     * 根据用户手机号 获取中心库中的 uid
     */
    @RequestMapping(value = "getUCenterId/{telephone}")
    public Object getUCenterId(@PathVariable String telephone, HttpServletRequest request) {
        if (StringUtils.isBlank(telephone)) {
            return Lists.newArrayList();
        }
        String regIp = userService.getRegip(request);

        List<String> telephoneList = Arrays.stream(telephone.split(","))
                .filter(StringUtils::isNoneBlank)
                .filter(mobile -> Pattern.matches(RegexConfig.MOBILE_PHONE_REGEX, mobile))
                .limit(TELEPHONE_MAX_SIZE)
                .collect(Collectors.toList());
        return thirdUtilService.getUCenterIdByPhone(telephoneList, regIp);
    }
}
