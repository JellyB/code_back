package com.huatu.tiku.interview.service;

import com.huatu.tiku.interview.entity.result.Result;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.interview.entity.po.User;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author: ZhenYang
 * @Date: Created in 2018/1/10 21:55
 * @Modefied By:
 */
public interface MobileService {
    /**
     * 验证Php
     *
     * @param mobile  手机号
     * @param openId
     * @param request session
     * @return
     */
    User checkPHP(String mobile, String openId, HttpServletRequest request);



    /**
     * 校验验证码
     *
     * @param mobile
     * @param captcha 验证码
     * @param request
     */
    Result userCaptcha(String mobile, String captcha, HttpServletRequest request);

    /** 发送验证码
     * @param mobile
     * @param clientIp ip，不知道什么用
     * @throws BizException
     */
    void sendCaptcha(User user,String mobile, String clientIp) throws BizException;
}
