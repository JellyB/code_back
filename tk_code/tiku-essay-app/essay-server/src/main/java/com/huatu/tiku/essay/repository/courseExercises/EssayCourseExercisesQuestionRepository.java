package com.huatu.tiku.essay.repository.courseExercises;

import com.huatu.tiku.essay.entity.courseExercises.EssayCourseExercisesQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/8/26
 * @描述 存储课程大纲绑定的课后作业
 */
@Repository
public interface EssayCourseExercisesQuestionRepository extends JpaRepository<EssayCourseExercisesQuestion, Long>, JpaSpecificationExecutor<EssayCourseExercisesQuestion> {

    /**
     * 根据课程id获取绑定试题信息
     *
     * @param courseWareId
     * @param status
     * @return
     */
    List<EssayCourseExercisesQuestion> findByCourseWareIdAndStatus(Long courseWareId, int status);

    /**
     * 根据课件id获取绑定的试题信息
     *
     * @param courseWareId
     * @param courseType
     * @param status
     * @return
     */
    List<EssayCourseExercisesQuestion> findByCourseWareIdAndCourseTypeAndStatusOrderBySort(Long courseWareId, Integer courseType, int status);


    List<EssayCourseExercisesQuestion> findByCourseWareIdAndCourseTypeAndPQidAndStatus(Long courseWareId, int courseType, Long pQid, int status);

    List<EssayCourseExercisesQuestion> findByCourseWareIdAndCourseTypeAndTypeAndStatus(Long courseWareId, int courseType, int type, int status);


    List<EssayCourseExercisesQuestion> findByCourseTypeAndCourseWareIdAndStatus(Integer courseType, Long syllabusId, int status);

    /**
     * 获取课件绑定试题数量
     */
    long countByCourseWareIdAndCourseTypeAndTypeAndStatus(Long courseWareId, Integer courseType, Integer type, Integer status);

    @Transactional
    @Modifying
    @Query("update EssayCourseExercisesQuestion  q set q.sort= ?1 where q.id=?2 and q.status=?3")
    int updateQuestionSort(Integer sort, Long id, Integer status);

    @Transactional
    @Modifying
    @Query(" update  EssayCourseExercisesQuestion q set q.status=?2 where  q.id =?1")
    int updateById(Long id, Integer status);


    @Transactional
    @Modifying
    @Query("update EssayCourseExercisesQuestion  q set q.status=?3 where q.courseWareId=?1 and q.courseType=?2")
    int updateStatus(Long courseWareId, int courseType, Integer status);

    /**
     * 根据ID查询记录信息
     *
     * @param id
     * @param status
     * @return
     */
    EssayCourseExercisesQuestion findByIdAndStatus(Long id, int status);

    /**
     * 根据meta中数据查询绑定记录
     * @param courseWareId
     * @param courseType
     * @param type
     * @param status
     * @param pQid
     * @return
     */
	 List<EssayCourseExercisesQuestion> findByCourseWareIdAndCourseTypeAndTypeAndStatusAndPQid(Long courseWareId, Integer courseType,
			Integer type, int status, Long pQid);


}
