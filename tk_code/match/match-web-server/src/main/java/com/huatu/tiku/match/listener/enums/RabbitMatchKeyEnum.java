package com.huatu.tiku.match.listener.enums;

import com.google.common.base.Joiner;
import com.huatu.tiku.match.constant.RabbitMatchKeyConstant;
import com.huatu.tiku.match.listener.impl.AnswerCardSubmitAsyncListener;
import com.huatu.tiku.match.listener.impl.MatchQuestionMetaSyncListener;
import com.huatu.tiku.match.listener.impl.MatchUserMetaSyncListener;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;

/**
 * Created by huangqingpeng on 2018/10/24.
 */
@AllArgsConstructor
@Getter
public enum  RabbitMatchKeyEnum {
    MatchUserMetaSync(RabbitMatchKeyConstant.MATCH_USER_META_SYNC,MatchUserMetaSyncListener.class ),
    MatchQuestionMetaSync(RabbitMatchKeyConstant.MATCH_QUESTION_META_SYNC,MatchQuestionMetaSyncListener.class ),
    AnswerCardSubmitAsync(RabbitMatchKeyConstant.ANSWER_CARD_SUBMIT_ASYNC,AnswerCardSubmitAsyncListener.class ),
    ;


    private String queueName;
    private Class listenerClass;

    public static RabbitMatchKeyEnum create(String queueName){
        for (RabbitMatchKeyEnum rabbitMatchKeyEnum : RabbitMatchKeyEnum.values()) {
            if(queueName.indexOf(rabbitMatchKeyEnum.getQueueName())==0){        //参数queueName是由name+env拼成的
                return rabbitMatchKeyEnum;
            }
        }
        return null;
    }

    public boolean equals(RabbitMatchKeyEnum rabbitMatchKeyEnum){
        return getQueueName().equals(rabbitMatchKeyEnum);
    }

    public static String getQueue(RabbitMatchKeyEnum rabbitMatchKeyEnum, String env) {
        if(null != rabbitMatchKeyEnum){
            return Joiner.on("_").join(rabbitMatchKeyEnum.getQueueName(),env);
        }
        return StringUtils.EMPTY;
    }
}
