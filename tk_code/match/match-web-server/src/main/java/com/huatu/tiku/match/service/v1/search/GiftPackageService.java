package com.huatu.tiku.match.service.v1.search;

import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.paper.bean.AnswerCard;

import java.util.Map;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-01-16 下午1:51
 **/
public interface GiftPackageService {



    /**
     * 处理大礼包url信息,模考列表
     * @param paperId
     * @return
     * @throws BizException
     */
    String obtainGiftIconUrl(int paperId) throws BizException;

    /**
     * 处理答题卡大礼包信息
     * @param answerCard
     * @param userName
     * @param token
     * @return
     * @throws BizException
     */
    AnswerCard buildGiftInfo4AnswerCard(AnswerCard answerCard, String userName, String token) throws BizException;

    /**
     * 是否领取过课程
     * @param userName
     * @param classId
     * @return
     * @throws BizException
     */
    Map<String, Boolean> checkCurrentClassHasReceived(String userName, String classId) throws BizException;

}
