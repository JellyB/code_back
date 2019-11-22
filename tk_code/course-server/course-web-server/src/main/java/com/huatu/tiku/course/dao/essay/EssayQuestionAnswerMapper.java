package com.huatu.tiku.course.dao.essay;

import com.huatu.tiku.course.dao.provider.CourseExercisesProcessEssayLogProvider;
import com.huatu.tiku.essay.entity.EssayQuestionAnswer;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.Map;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-09-03 7:54 PM
 **/
@Repository
public interface EssayQuestionAnswerMapper extends Mapper<EssayQuestionAnswer>{


    /**
     * 根据 question answer id 查询
     * @param id
     * @return
     */
    @SelectProvider(type = CourseExercisesProcessEssayLogProvider.class, method = "selectQuestionAnswerById")
    Map<String, Object> selectQuestionAnswerById(@Param(value = "id") Long id);
}
