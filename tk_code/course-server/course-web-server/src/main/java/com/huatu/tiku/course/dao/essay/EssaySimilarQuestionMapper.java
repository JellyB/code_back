package com.huatu.tiku.course.dao.essay;

import com.huatu.tiku.course.dao.provider.CourseExercisesProcessEssayLogProvider;
import com.huatu.tiku.essay.entity.EssaySimilarQuestion;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.Map;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-08-31 6:13 PM
 **/
@Repository
public interface EssaySimilarQuestionMapper extends Mapper<EssaySimilarQuestion>{

    /**
     * 根据 questionBaseId 查询 similarId
     * @param questionBaseId
     * @return
     */
    @SelectProvider(type = CourseExercisesProcessEssayLogProvider.class, method = "selectByQuestionBaseId")
    Map<String, Object> selectByQuestionBaseId(@Param(value = "questionBaseId") Long questionBaseId);
}
