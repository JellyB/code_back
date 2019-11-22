package com.huatu.tiku.course.dao.essay;

import com.huatu.tiku.course.dao.provider.CourseExercisesProcessEssayLogProvider;
import com.huatu.tiku.essay.entity.EssayQuestionBase;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.Map;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-09-02 1:48 PM
 **/

@Repository
public interface EssayQuestionBaseMapper extends Mapper<EssayQuestionBase>{

    @SelectProvider(type = CourseExercisesProcessEssayLogProvider.class, method = "selectQuestionBaseById")
    Map<String, Object> selectQuestionBaseById(@Param(value = "questionBaseId") Long questionBaseId);
}
