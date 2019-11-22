package com.huatu.tiku.essay.service.v2.question;

import com.huatu.tiku.essay.entity.EssaySimilarQuestionGroupInfo;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.service.v2.impl.question.SingleQuestionSearchImplV2;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.vo.resp.EssayQuestionVO;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Created by lijun on 2018/10/26
 */
public interface SingleQuestionSearchV2 {

    /**
     * 分页查询 所有的单题组列表
     *
     * @param pageRequest 分页参数
     * @param type        类型
     * @return 查询结果
     */
    PageUtil<List<EssaySimilarQuestionGroupInfo>> findSimilarQuestionPageInfo(Pageable pageRequest, int type);

    /**
     * 查询 包含数据统计的 列表
     *
     * @param similarQuestionGroupInfoList 单题组列表
     * @param userId                       用户ID
     * @param normal
     * @return 查询结果
     */
    List<EssayQuestionVO> findSimilarQuestionList(List<EssaySimilarQuestionGroupInfo> similarQuestionGroupInfoList, int userId, EssayAnswerCardEnum.ModeTypeEnum normal);

    /**
     * 获取用户单题智能 & 人工批改次数
     * @param userId
     * @param questionIds
     * @param modeTypeEnum
     * @return
     */
    SingleQuestionSearchImplV2.UserCorrectModeInfo invoke(int userId, List<Long> questionIds, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum);

}
