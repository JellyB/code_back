package com.huatu.tiku.essay.repository.courseExercises;


import com.huatu.tiku.essay.entity.courseExercises.EssayExercisesAnswerMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/8/26
 * @描述 课后练习跟答题卡关联表
 */
@Repository
public interface EssayExercisesAnswerMetaRepository extends JpaRepository<EssayExercisesAnswerMeta, Long>, JpaSpecificationExecutor<EssayExercisesAnswerMeta> {

    List<EssayExercisesAnswerMeta> findByUserIdAndPQidAndAnswerTypeAndSyllabusIdAndStatus(Integer userId, Long paperOrQuestionId, Integer type, Long syllabusId, Integer status);

    List<EssayExercisesAnswerMeta> findByAnswerIdAndAnswerTypeAndStatus(Long answerId, Integer answerType, Integer status);

    List<EssayExercisesAnswerMeta> findBySyllabusIdAndUserIdAndStatus(Long syllabusId, Integer userId, int status);

    //List<EssayExercisesAnswerMeta> findByCourseWareIdAndUserIdAndStatus(Long courseWareId, Integer userId, int status);

    List<EssayExercisesAnswerMeta> findByPQidAndAnswerTypeAndSyllabusIdAndStatus(Long paperOrQuestionId, Integer type, Long syllabusId, Integer status);

    /**
     * 根据答题卡和类型查询答题记录
     *
     * @param paperOrQuestionId
     * @param type
     * @param status
     * @return
     */
    List<EssayExercisesAnswerMeta> findByAnswerIdInAndAnswerTypeAndStatus(List<Long> answerIdList, Integer type, Integer status);

    /**
     * 根据课程id获取做过的答题记录
     *
     * @param courseId
     * @param intValue
     * @param code
     * @return
     */
    List<EssayExercisesAnswerMeta> findByCourseIdAndUserIdAndStatus(long courseId, int userId, int status);

    /**
     * 查询某个用户指定课件下所有答题卡
     *
     * @param pQid
     * @param answerType
     * @param syllabusId
     * @param userId
     * @param status
     * @return
     */
    List<EssayExercisesAnswerMeta> findByPQidAndAnswerTypeAndSyllabusIdAndUserIdAndStatus(Long pQid, Integer answerType,
                                                                                          Long syllabusId, Integer userId, int status);


    List<EssayExercisesAnswerMeta> findByUserIdAndPQidAndAnswerTypeAndSyllabusIdAndCorrectNumAndStatus(Integer userId, Long paperOrQuestionId, Integer type, Long syllabusId, Integer correctNum, Integer status);


    /**
     * 根据大纲id和试题id获取班级答题信息
     *
     * @param paperOrQuestionId
     * @param type
     * @param syllabusId
     * @param status
     * @return
     */
    List<EssayExercisesAnswerMeta> findByPQidAndAnswerTypeAndSyllabusIdAndStatusAndBizStatus(Long paperOrQuestionId, Integer type, Long syllabusId, Integer status, Integer bizStatus);

    /**
     * 根据课件 id 课件类型查询
     * @param courseWareId
     * @param courseType
     * @param status
     * @return
     */
    List<EssayExercisesAnswerMeta> findByCourseWareIdAndCourseTypeAndStatus(Long courseWareId, Integer courseType, Integer status);


}
