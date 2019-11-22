package com.huatu.ztk.report.task;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.report.service.QuestionSummaryService;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * 提交试卷通知
 * Created by shaojieyue
 * Created time 2016-05-28 21:46
 */
public class QuestionAnswerMessageListener implements MessageListener {
    private static final Logger logger = LoggerFactory.getLogger(QuestionAnswerMessageListener.class);

    @Autowired
    private QuestionSummaryService questionSummaryService;
    public void onMessage(Message message) {
        String text = new String(message.getBody());
        logger.info("receive message={}",text);
        final Map data = JsonUtil.toMap(text);
        long uid = MapUtils.getLongValue(data,"uid");
        int subject = MapUtils.getIntValue(data,"subject");
        int wsum = MapUtils.getIntValue(data,"wsum");
        int rsum = MapUtils.getIntValue(data,"rsum");
        int times = MapUtils.getIntValue(data,"times");
        int area = MapUtils.getIntValue(data,"area");
        questionSummaryService.update(uid,subject,wsum,rsum,times,area);

    }
}
