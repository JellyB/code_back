package com.huatu.tiku.interview.service;


import com.huatu.tiku.interview.entity.template.TemplateMsgResult;

/**
 * @Author ZhenYang
 * @Date Created in 2018/1/12 16:33
 * @Description
 */
public interface WechatTemplateMsgService {
    TemplateMsgResult sendTemplate( String data);
}
