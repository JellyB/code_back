package com.huatu.tiku.course.dao.essay;

import com.huatu.tiku.course.dao.provider.CourseExercisesProcessEssayLogProvider;
import com.huatu.tiku.essay.entity.courseExercises.EssayExercisesAnswerMeta;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-08-31 1:45 PM
 **/

@Repository
public interface EssayExercisesAnswerMetaMapper extends Mapper<EssayExercisesAnswerMeta> {

    /**
     * 获取课后作业分页详情
     * @param userId
     * @param page
     * @param size
     * @return
     */
    @SelectProvider(type = CourseExercisesProcessEssayLogProvider.class, method = "getEssayCoursePageInfo")
    List<HashMap<String, Object>> getEssayCoursePageInfo(long userId, int page, int size);


    /**
     * 查询学员未完成课后作业大纲id
     * @param userId
     * @return
     */
    @SelectProvider(type = CourseExercisesProcessEssayLogProvider.class, method = "selectDistinctSyllabusIdByUserId")
    Set<Long> selectDistinctSyllabusIdByUserId(@Param(value = "userId") int userId);

    /**
     *
     * @param answerCardId
     * @return
     */
    @SelectProvider(type = CourseExercisesProcessEssayLogProvider.class, method = "getBizStatusByCardId")
    HashMap<String, Object> getBizStatusByCardId(@Param(value = "answerCardId") long answerCardId);

    /**
     *
     * @param syllabusId
     * @return
     */
    @SelectProvider(type = CourseExercisesProcessEssayLogProvider.class, method = "getAnswerCardInfoBySyllabusId")
    HashMap<String, Object> getAnswerCardInfoBySyllabusId(int userId, long syllabusId);

    /**
     * 获取用户多道未做完单题数
     * @return
     */
    @Deprecated
    @SelectProvider(type = CourseExercisesProcessEssayLogProvider.class, method = "selectUnDoQuestionCountBySyllabusId")
    Map<String,Object> selectUnDoQuestionCountBySyllabusId(int userId, long syllabusId);


    /**
     * 查询当前用户 correct num
     * @return
     */
    @SelectProvider(type = CourseExercisesProcessEssayLogProvider.class, method = "selectCurrentCorrectNum")
    Map<String, Object> selectCurrentCorrectNum(int userId, long syllabusId);

    /**
     * 获取不同答题卡 status count
     * @param userId
     * @param syllabusId
     * @param correctNum
     * @return
     */
    @SelectProvider(type = CourseExercisesProcessEssayLogProvider.class, method = "selectMultiQuestionBizStatusCount")
    List<Map<String, Object>> selectMultiQuestionBizStatusCount(int userId, long syllabusId, int correctNum);
    
    /**
     * 获取多题答题卡 status count
     * @param userId
     * @param syllabusId
     * @param correctNum
     * @return
     */
    @SelectProvider(type = CourseExercisesProcessEssayLogProvider.class, method = "selectMultiBizStatusCount")
    List<Map<String, Object>> selectMultiBizStatusCount(int userId, long syllabusId);
}
