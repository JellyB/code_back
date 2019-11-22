package com.huatu.tiku.course.dao.essay;

import com.huatu.tiku.course.dao.provider.CourseExercisesProcessEssayLogProvider;
import com.huatu.tiku.essay.entity.EssayPaperBase;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.Map;


/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-09-02 1:57 PM
 **/
@Repository
public interface EssayPaperBaseMapper extends Mapper<EssayPaperBase> {


    @SelectProvider(type = CourseExercisesProcessEssayLogProvider.class, method = "selectPaperBaseById")
    Map<String, Object> selectPaperBaseById(@Param(value = "paperBaseId") Long paperBaseId);
}
