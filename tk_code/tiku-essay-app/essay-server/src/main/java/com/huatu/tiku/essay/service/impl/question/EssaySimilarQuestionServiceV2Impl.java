package com.huatu.tiku.essay.service.impl.question;

import com.huatu.tiku.essay.entity.*;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.repository.EssaySimilarQuestionGroupInfoRepository;
import com.huatu.tiku.essay.service.EssayQuestionService;
import com.huatu.tiku.essay.service.EssaySimilarQuestionService;
import com.huatu.tiku.essay.service.v2.impl.question.SingleQuestionSearchImplV2;
import com.huatu.tiku.essay.service.v2.question.EssaySimilarQuestionServiceV2;
import com.huatu.tiku.essay.service.v2.question.SingleQuestionSearchV2;
import com.huatu.tiku.essay.vo.resp.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 单题组管理
 *
 * @author zhaoxi
 */
@Service
@Slf4j
public class EssaySimilarQuestionServiceV2Impl implements EssaySimilarQuestionServiceV2 {
    @Autowired
    EssayQuestionService essayQuestionService;

    @Autowired
    private SingleQuestionSearchV2 singleQuestionSearch;

    @Autowired
    private EssaySimilarQuestionService essaySimilarQuestionService;

    @Autowired
    private EssaySimilarQuestionGroupInfoRepository essaySimilarQuestionGroupInfoRepository;



    @Override
    public List<EssayQuestionAreaVO> findAreaList(long similarId, int userId, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) {
        EssaySimilarQuestionGroupInfo similarQuestion = essaySimilarQuestionGroupInfoRepository.findOne(similarId);

        EssayQuestionVO vo = EssayQuestionVO.builder()
                .similarId(similarQuestion.getId())
                .showMsg(similarQuestion.getShowMsg())
                .build();

        //查询该试题  （所属地区年份试卷列表，存在base表中）（走缓存）
        List<EssayQuestionAreaVO> result = essaySimilarQuestionService.findAreaList(similarQuestion);

        if(CollectionUtils.isEmpty(result)){
            return result;
        }
        List<Long> questionIds = result.stream().map(EssayQuestionAreaVO::getQuestionBaseId).collect(Collectors.toList());
        SingleQuestionSearchImplV2.UserCorrectModeInfo userCorrectModeInfo = singleQuestionSearch.invoke(userId, questionIds, modeTypeEnum);

        Map<Long, Long> userIntelligenceCorrectNumMap = userCorrectModeInfo.getUserIntelligenceCorrectNumMap();
        Map<Long, Long> userManualCorrectNumMap = userCorrectModeInfo.getUserManualCorrectNumMap();
        result.forEach(item -> {
            Long questionId = item.getQuestionBaseId();
            int currentUserCorrectNum = MapUtils.getIntValue(userIntelligenceCorrectNumMap, questionId, 0);
            int currentUserManualNum = MapUtils.getIntValue(userManualCorrectNumMap, questionId, 0);
            item.setCorrectNum(currentUserCorrectNum);
            item.setManualNum(currentUserManualNum);
        });
        return result;
    }
}
