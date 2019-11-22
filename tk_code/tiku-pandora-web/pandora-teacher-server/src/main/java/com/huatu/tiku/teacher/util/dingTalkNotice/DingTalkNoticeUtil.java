package com.huatu.tiku.teacher.util.dingTalkNotice;

import com.google.common.collect.Lists;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.paper.bean.AnswerCard;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/5/29
 * @描述
 */

@Component
@Slf4j
public class DingTalkNoticeUtil {

    @Value("${dingding.webhook.token}")
    private String webHook_token;

    private static final Logger logger = LoggerFactory.getLogger(DingTalkNoticeUtil.class);

    /**
     * link方式
     *
     * @param dingLinkVo
     */
    public void linkNotice(DingLinkVo dingLinkVo) {

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<DingLinkVo> responseResult = restTemplate.postForEntity(webHook_token, dingLinkVo, DingLinkVo.class);
        if (responseResult.getStatusCode() != HttpStatus.OK) {
            logger.info("钉钉发送失败，信息是:{}", responseResult);
        }
    }

    /**
     * text方式
     *
     * @param baseVo
     */
    public void textNotice(DingBaseVo baseVo) {

        if (null == baseVo) {
            logger.info("钉钉通知消息为空");
        }
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<DingTextVo> responseResult = restTemplate.postForEntity(webHook_token, baseVo, DingTextVo.class);
        if (responseResult.getStatusCode() != HttpStatus.OK) {
            logger.info("钉钉通知发送失败，信息是:{}", responseResult);
        }
    }

}
