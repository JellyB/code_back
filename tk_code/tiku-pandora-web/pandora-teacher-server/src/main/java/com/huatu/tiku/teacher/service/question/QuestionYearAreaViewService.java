package com.huatu.tiku.teacher.service.question;

import com.huatu.tiku.dto.QuestionYearAreaDTO;

import java.util.List;

/**
 * Created by lijun on 2018/8/24
 */
public interface QuestionYearAreaViewService {

    /**
     * 根据ID 查询
     *
     * @param questionId 试题ID
     * @return
     */
    QuestionYearAreaDTO selectByPrimaryKey(Long questionId);

    /**
     * 试题ID 信息
     *
     * @param areaIdList 区域集合
     * @param year       年份
     * @return 试题ID
     */
    List<Long> selectQuestionId(List<Long> areaIdList, Integer year);

}
