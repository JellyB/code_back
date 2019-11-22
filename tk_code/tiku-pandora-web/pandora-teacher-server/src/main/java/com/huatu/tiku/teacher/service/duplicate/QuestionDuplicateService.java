package com.huatu.tiku.teacher.service.duplicate;

import com.huatu.tiku.entity.duplicate.QuestionDuplicate;
import com.huatu.tiku.service.BaseService;

import java.util.List;
import java.util.Map;

/**
 * 试题 - 复用数据 - 关联关系
 * Created by huangqp on 2018\7\13 0013.
 */
public interface QuestionDuplicateService extends BaseService<QuestionDuplicate> {
    /**
     * 查询试题自身及所有连带试题
     *
     * @param questionId
     * @return
     */
    List<Map> findWithDuplicateByQuestionId(Long questionId);

    /**
     * 根据复用id查询使用该复用数据的所有试题
     *
     * @param duplicateId
     * @return
     */
    List<Map> findWithDuplicateByDuplicateId(Long duplicateId);

}
