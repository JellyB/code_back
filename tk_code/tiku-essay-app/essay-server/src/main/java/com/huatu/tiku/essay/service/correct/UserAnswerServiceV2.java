package com.huatu.tiku.essay.service.correct;

import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.vo.resp.EssayAnswerV2VO;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-07-11 4:50 PM
 **/
public interface UserAnswerServiceV2 {

    /**
     * 套题批改记录
     * @param userId
     * @param pageRequest
     * @param normal
     * @return
     */
    List<EssayAnswerV2VO> correctPaperList(int userId, Pageable pageRequest, EssayAnswerCardEnum.ModeTypeEnum normal);


    /**
     * 套题批改记录 count
     * @param userId
     * @param normal
     * @return
     */
    long countCorrectPaperList(int userId, EssayAnswerCardEnum.ModeTypeEnum normal);


    List<EssayAnswerV2VO> questionCorrectList(int userId, Integer type, EssayAnswerCardEnum.ModeTypeEnum normal, Pageable pageRequest);

}
