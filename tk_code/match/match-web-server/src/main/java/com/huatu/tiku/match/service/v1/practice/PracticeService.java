package com.huatu.tiku.match.service.v1.practice;

import com.huatu.tiku.match.bo.paper.StandAnswerCardBo;
import com.huatu.ztk.commons.exception.BizException;

/**
 * 描述：模考大赛 答题卡接口
 *
 * @author biguodong
 *         Create time 2018-10-24 下午5:51
 **/
public interface PracticeService {

    /**
     * 获取答题卡信息
     * @param paperId
     * @param userId
     * @param userName
     * @param token
     * @return
     * @throws BizException
     */
    StandAnswerCardBo getUserAnswerCard(int paperId, int userId, String userName, String token,String cv,int terminal) throws BizException;
}
