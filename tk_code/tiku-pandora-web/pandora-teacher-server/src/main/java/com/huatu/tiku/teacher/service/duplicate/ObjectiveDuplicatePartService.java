package com.huatu.tiku.teacher.service.duplicate;

import com.huatu.tiku.entity.duplicate.ObjectiveDuplicatePart;
import com.huatu.tiku.response.question.DuplicatePartResp;
import com.huatu.tiku.service.BaseService;

import java.util.HashMap;
import java.util.List;

/**
 * Created by huangqp on 2018\5\16 0016.
 */
public interface ObjectiveDuplicatePartService extends BaseService<ObjectiveDuplicatePart> {

    /**
     * 客观题 - 复用数据 -添加（携带filter字段）
     * @param objectiveDuplicatePart
     */
    void insertWithFilter(ObjectiveDuplicatePart objectiveDuplicatePart);

    /**
     * 客观题 - 复用数据 -修改（携带filter字段）
     * @param objectiveDuplicatePart
     */
    void updateWithFilter(ObjectiveDuplicatePart objectiveDuplicatePart);

    /**
     * 客观题 - 查重逻辑
     * @param choiceContent
     * @param stem
     * @param analysis
     * @param extend
     * @param questionType
     * @return
     */
    List<DuplicatePartResp> selectByMyExample(String choiceContent, String stem, String analysis, String extend, Integer questionType);

    /**
     * 查询客观复用数据的
     * @param questionId  试题ID
     * @return  复用数据 (题干+选项+答案+解析+拓展)
     */
    HashMap<String,Object> findByQuestionId(Long questionId);
}
