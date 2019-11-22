package com.huatu.tiku.teacher.service.question;

import com.huatu.tiku.entity.common.QuestionType;
import com.huatu.tiku.service.BaseService;

/**
 * Created by huangqp on 2018\6\15 0015.
 */
public interface QuestionTypeService extends BaseService<QuestionType> {
    /**
     * 根据科目查询题型
     *
     * @param subjectId
     * @return
     */
    Object findTypeBySubject(Long subjectId);

    /**
     * 通过题型名称查询试题题型id
     *
     * @param name
     * @return
     */
    Long findIdByName(String name);
}
