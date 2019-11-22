package com.huatu.tiku.course.dao.manual;

import com.huatu.tiku.course.dao.provider.CourseExercisesProcessLogProvider;
import com.huatu.tiku.entity.CourseExercisesProcessLog;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.HashMap;
import java.util.List;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-02-25 下午6:13
 **/
@Repository
public interface CourseExercisesProcessLogMapper extends Mapper<CourseExercisesProcessLog> {

    /**
     * 获取课后作业分页详情
     * @param userId
     * @param page
     * @param size
     * @return
     */
    @SelectProvider(type = CourseExercisesProcessLogProvider.class, method = "getCoursePageInfo")
    List<HashMap<String, Object>> getCoursePageInfo(long userId, int page, int size);


    /**
     * 查询错误数据
     * @return
     */
    @SelectProvider(type = CourseExercisesProcessLogProvider.class, method = "getDuplicateDate")
    List<HashMap<String, Object>> getDuplicateDate();

    @SelectProvider(type = CourseExercisesProcessLogProvider.class, method = "distinctCourseId")
    List<Integer> distinctCourseId();

    @SelectProvider(type = CourseExercisesProcessLogProvider.class, method = "summaryData")
    List<HashMap<Integer, Integer>> summaryData(@Param(value = "courseId") Integer courseId);
}
