package com.huatu.tiku.teacher.service.duplicate;

import com.huatu.tiku.entity.duplicate.SubjectiveDuplicatePart;
import com.huatu.tiku.response.question.DuplicatePartResp;
import com.huatu.tiku.service.BaseService;

import java.util.HashMap;
import java.util.List;

/**
 * 主观题 - 复用数据
 * Created by huangqp on 2018\6\28 0028.
 */
public interface SubjectiveDuplicatePartService extends BaseService<SubjectiveDuplicatePart> {

    /**
     * 主观题-复用数据-添加（携带filter）
     * @param subjectiveDuplicatePart
     * @return
     */
    int insertWithFilter(SubjectiveDuplicatePart subjectiveDuplicatePart);

    /**
     * 主观题-复用数据-修改（携带filter）
     * @param subjectiveDuplicatePart
     * @return
     */
    int updateWithFilter(SubjectiveDuplicatePart subjectiveDuplicatePart);

    /**
     * 主观题-查重逻辑
     * @param stem
     * @param extend
     * @param answerComment
     * @param analyzeQuestion
     * @param answerRequest
     * @param bestowPointExplain
     * @param trainThought
     * @param omnibusRequirements
     * @param questionType
     * @return
     */
    List<DuplicatePartResp> selectByMyExample(String stem, String extend, String answerComment, String analyzeQuestion, String answerRequest, String bestowPointExplain, String trainThought, String omnibusRequirements, Integer questionType);

    /**
     * 复用数据查询
     * @param id  试题id
     * @return 试题内容
     */
    HashMap<String,Object> findByQuestionId(long id);


}
