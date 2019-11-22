package com.huatu.tiku.interview.service;

import com.huatu.tiku.interview.entity.result.Result;
import com.huatu.tiku.interview.entity.vo.request.PaperCommitVO;

/**
 * Created by x6 on 2018/4/11.
 */
public interface ClassInteractionService {
    //查询试卷详情
    Object getPaperDetail(String openId, long paperId);

    Result answer(PaperCommitVO vo);

    Object getPaperDetailV2(String openId, long paperId, long pushId);

    Result answerV2(PaperCommitVO vo);

    Object getPaperDetailV3(String openId, long paperId, long pushId);

}
