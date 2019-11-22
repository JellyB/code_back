package com.huatu.tiku.course.dao.essay;

import com.huatu.tiku.course.dao.provider.CourseExercisesProcessEssayLogProvider;
import com.huatu.tiku.essay.entity.EssayQuestionDetail;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.Map;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-09-02 1:53 PM
 **/
@Repository
public interface EssayQuestionDetailMapper extends Mapper<EssayQuestionDetail>{

    @SelectProvider(type = CourseExercisesProcessEssayLogProvider.class, method = "selectQuestionDetailById")
    Map<String, Object> selectQuestionDetailById(@Param(value = "detailId") Long detailId);
}
