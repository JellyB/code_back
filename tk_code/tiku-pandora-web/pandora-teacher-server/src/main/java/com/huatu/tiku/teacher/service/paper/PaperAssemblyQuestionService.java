package com.huatu.tiku.teacher.service.paper;

import com.huatu.tiku.entity.question.PaperQuestionSimpleInfo;
import com.huatu.tiku.entity.question.QuestionSimpleInfo;
import com.huatu.tiku.entity.teacher.PaperAssemblyQuestion;
import com.huatu.tiku.service.BaseService;

import java.util.List;

/**
 * Created by lijun on 2018/8/16
 */
public interface PaperAssemblyQuestionService extends BaseService<PaperAssemblyQuestion> {

    /**
     * 保存组卷-试题信息
     *
     * @param paperId        试卷ID
     * @param questionIdList 试题ID集合
     * @return 成功数量
     */
    int saveQuestionInfo(Long paperId, List<Long> questionIdList);

    /**
     * 试题详情列表
     *
     * @param paperId 试卷ID
     * @return 列表
     */
    List<QuestionSimpleInfo> list(Long paperId);
}
