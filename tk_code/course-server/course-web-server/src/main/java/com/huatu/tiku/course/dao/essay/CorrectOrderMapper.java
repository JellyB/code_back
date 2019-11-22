package com.huatu.tiku.course.dao.essay;

import com.huatu.tiku.course.dao.provider.CourseExercisesProcessEssayLogProvider;
import com.huatu.tiku.essay.entity.correct.CorrectOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.Map;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-09-02 4:36 PM
 **/
@Repository
public interface CorrectOrderMapper extends Mapper<CorrectOrder>{

    /**
     * 获取被退回信息
     * @param answerCardType
     * @param answerCardId
     * @return
     */
    @SelectProvider(type = CourseExercisesProcessEssayLogProvider.class, method = "selectByAnswerCardIdAndType")
    Map<String, Object> selectByAnswerCardIdAndType(int answerCardType, long answerCardId);

}
