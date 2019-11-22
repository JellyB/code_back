package com.huatu.tiku.schedule.biz.repository;

import com.huatu.tiku.schedule.base.repository.BaseRepository;
import com.huatu.tiku.schedule.biz.domain.Course;
import com.huatu.tiku.schedule.biz.domain.Teacher;
import com.huatu.tiku.schedule.biz.enums.CourseStatus;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Transient;
import java.util.List;

/**
 * 课程Repository
 * 
 * @author Geek-S
 *
 */
public interface CourseRepository extends BaseRepository<Course, Long> {

    @Query(value = "select distinct c.* from course c left join course_live cl on c.id=cl.course_id where cl.id in ?1",nativeQuery = true)
    List<Course> findAllByLives(List<Long> liveIds);


    /**
     * 更改课程状态 根据id和旧状态修改为新状态
     * @param courseId 课程状态
     * @param oldStatus 旧状态
     * @param newStatus 新状态
     * @return 结果
     */
    @Transactional
    @Modifying
    @Query(value = "update course set status = ?3 where id = ?1 and status =?2",nativeQuery = true)
    int updateCourseStatusById(Long courseId,int oldStatus,int newStatus);

    /**
     * 根据课程id查找课程状态
     * @param courseId 课程直播id
     * @return 课程状态
     */
    @Query(value = "SELECT status FROM course  WHERE id = ?1",nativeQuery = true)
    CourseStatus findCourseStatusByCourseId(Long courseId);

    /**
     * 根据课程id查找课程科目id
     * @param courseLiveId
     * @return 科目id
     */
    @Query(value = "select c.subject_id from course c LEFT JOIN course_live cl on cl.course_id=c.id where cl.id=?1",nativeQuery = true)
    Long getCourseSubjectId(Long courseLiveId);

    /**
     * 添加面试课程推荐教师
     * @param courseId 课程id
     * @param teacherId 教师id
     */
    @Transactional
    @Modifying
    @Query(value = "INSERT  INTO course_interview_teacher (course_id, interview_teacher) values (?1, ?2)",nativeQuery = true)
    void saveInterviewTeacherId(Long courseId, Long teacherId);


}
