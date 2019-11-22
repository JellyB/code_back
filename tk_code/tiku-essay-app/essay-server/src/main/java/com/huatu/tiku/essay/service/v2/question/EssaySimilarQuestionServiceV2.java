package com.huatu.tiku.essay.service.v2.question;

import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.vo.resp.EssayQuestionAreaVO;

import java.util.List;

/**
 * Created by x6 on 2017/12/19.
 */
public interface EssaySimilarQuestionServiceV2 {

    /**
     * 获取地区信息 & 补充智能批改次数 & 人工批改次数
     * @param similarId
     * @param userId
     * @param normal
     * @return
     */
    List<EssayQuestionAreaVO> findAreaList(long similarId, int userId, EssayAnswerCardEnum.ModeTypeEnum normal);
}
