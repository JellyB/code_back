package com.huatu.tiku.teacher.service.question;

import com.huatu.tiku.dto.DuplicateQuestionVo;
import com.huatu.tiku.entity.duplicate.QuestionDuplicate;
import com.huatu.tiku.response.question.DuplicatePartResp;
import com.huatu.tiku.service.BaseService;

import java.util.List;

/**
 * @创建人 lizhenjuan
 * @创建时间 2018/9/10
 * @描述 处理试题查重相关逻辑
 */
public interface DuplicateQuestionService extends BaseService<QuestionDuplicate> {


    /**
     * ·1
     * 根据题型做去重查询
     *
     * @return 复用内容
     */
    List<DuplicatePartResp> findDuplicatePartFromEs(DuplicateQuestionVo duplicateQuestionVo, int page, int size, Long subjectId, int score);

    /**
     * 根据试题ID获取所有的试题信息
     *
     * @param
     * @param questionIds
     * @return
     */
    List<DuplicatePartResp> assembleQuestionInfo(List<Long> questionIds, int questionType);

    /**
     * 获取科目名称
     *
     * @param question
     * @return
     */
    String getSubjectName(DuplicatePartResp question);

}
