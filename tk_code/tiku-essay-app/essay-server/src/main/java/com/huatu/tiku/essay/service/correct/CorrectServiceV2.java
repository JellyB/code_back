package com.huatu.tiku.essay.service.correct;

import com.huatu.common.exception.BizException;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-07-11 7:55 PM
 **/
public interface CorrectServiceV2 {

    /**
     * 智能转人工批改
     * @param answerCardId
     * @param type
     * @param normal
     * @return
     */
    Object convert(Long answerCardId, Integer type, Integer delayStatus, UserSession userSession, EssayAnswerCardEnum.ModeTypeEnum normal) throws BizException;
}
