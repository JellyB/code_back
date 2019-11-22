package com.huatu.tiku.schedule.biz.repository;

import java.util.Date;
import java.util.List;

import com.huatu.tiku.schedule.base.repository.BaseRepository;
import com.huatu.tiku.schedule.biz.domain.CourseLive;
import com.huatu.tiku.schedule.biz.domain.Teacher;
import com.huatu.tiku.schedule.biz.enums.CourseConfirmStatus;
import com.huatu.tiku.schedule.biz.enums.CourseLiveCategory;
import com.huatu.tiku.schedule.biz.enums.CourseStatus;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * 课程直播Repository
 *
 * @author Geek-S
 */
public interface CourseLiveRepository extends BaseRepository<CourseLive, Long> {

    /**
     * 判断课程直播是否已经存在
     *
     * @param courseId  课程ID
     * @param date      日期
     * @param timeBegin 开始时间
     * @param timeEnd   结束时间
     * @return true/false
     */
    Boolean existsByCourseIdAndDateAndTimeBeginAndTimeEnd(Long courseId, Date date, Integer timeBegin, Integer timeEnd);

    /**
     * 获取课程直播
     *
     * @param courseId  课程ID
     * @param date      日期
     * @param timeBegin 开始时间
     * @param timeEnd   结束时间
     * @return 课程直播
     */
    CourseLive findOneByCourseIdAndDateAndTimeBeginAndTimeEnd(Long courseId, Date date, Integer timeBegin,
                                                              Integer timeEnd);

    /**
     * 根据教师id和未确认状态查询直播
     *
     * @param teacherId 教师id
     * @return 直播结合
     */
    @Query(value = "select * from course_live cl LEFT JOIN course_live_teacher clt on cl.id=clt.course_live_id left join course c on c.id=cl.course_id where clt.teacher_id=?1 and clt.confirm=?2 and c.status>=?3", nativeQuery = true)
    List<CourseLive>  findTaskTeacherByTeacherId(Long teacherId,int confirm,int status);

    //通过课程状态助教和助教确认状态查找直播
    @Query(value = "select * from course_live cl LEFT JOIN  course c on c.id=cl.course_id where cl.assistant_id=?1 and cl.ass_confirm=?2 and c.status=?3",nativeQuery = true)
    List<CourseLive> getAllByAssistantAndAssConfirm(Long teacherId, int confirm,int status);

    //通过课程状态学习师和学习师确认状态查找直播
    @Query(value = "select * from course_live cl LEFT JOIN  course c on c.id=cl.course_id where cl.learning_teacher_id=?1 and cl.lt_confirm=?2 and c.status=?3",nativeQuery = true)
    List<CourseLive> getAllByLearningTeacherAndLtConfirm(Long teacherId,int confirm,int status);

    //通过课程状态场控和场控确认状态查找直播
    @Query(value = "select * from course_live cl LEFT JOIN  course c on c.id=cl.course_id where cl.controller_id=?1 and cl.ctrl_confirm=?2 and c.status=?3",nativeQuery = true)
    List<CourseLive> getAllByControllerAndCtrlConfirm(Long teacherId, int confirm,int status);

    //通过课程状态主持人和主持人确认状态查找直播
    @Query(value = "select * from course_live cl LEFT JOIN  course c on c.id=cl.course_id where cl.compere_id=?1 and cl.com_confirm=?2 and c.status=?3",nativeQuery = true)
    List<CourseLive> getAllByCompereAndComConfirm(Long teacherId, int confirm,int status);


    /**
     * 助教任务确认
     *
     * @param teacherId 教师id
     * @param ids       教师直播id集合
     * @param courseConfirmStatus 确认状态
     * @return 确认个数
     */
    @Modifying
    @Transactional
    @Query(value = "update CourseLive cl set cl.assConfirm = ?3 where cl.id in ?2 and cl.assistant.id=?1")
    int updateTaskAssistant(Long teacherId, List<Long> ids, CourseConfirmStatus courseConfirmStatus);

    /**
     * 学习师任务确认
     *
     * @param teacherId 教师id
     * @param ids       教师直播id集合
     * @param courseConfirmStatus 确认状态
     * @return 确认个数
     */
    @Modifying
    @Transactional
    @Query(value = "update CourseLive cl set cl.ltConfirm = ?3 where cl.id in ?2 and cl.learningTeacher.id=?1")
    int updateTaskLearningTeacher(Long teacherId, List<Long> ids, CourseConfirmStatus courseConfirmStatus);

    /**
     * 场控任务确认
     *
     * @param teacherId 教师id
     * @param ids       教师直播id集合
     * @param courseConfirmStatus 确认状态
     * @return 确认个数
     */
    @Modifying
    @Transactional
    @Query(value = "update CourseLive cl set cl.ctrlConfirm = ?3 where cl.id in ?2 and cl.controller.id=?1")
    int updateTaskController(Long teacherId, List<Long> ids, CourseConfirmStatus courseConfirmStatus);

    /**
     * 主持人任务确认
     *
     * @param teacherId 教师id
     * @param ids       教师直播id集合
     * @param courseConfirmStatus 确认状态
     * @return 确认个数
     */
    @Modifying
    @Transactional
    @Query(value = "update CourseLive cl set cl.comConfirm = ?3 where cl.id in ?2 and cl.compere.id=?1")
    int updateTaskCompere(Long teacherId, List<Long> ids, CourseConfirmStatus courseConfirmStatus);

    /**
     * 修改直播绑定直播间
     *
     * @param roomId 直播间id
     * @param liveId 直播id
     * @return
     */
    @Modifying
    @Transactional
    @Query(value = "update CourseLive cl set cl.liveRoomId = ?1 where cl.id = ?2")
    int updateLiveRoom(Long roomId, Long liveId);

    /**
     * 修改直播绑定助教
     * @param liveId 直播id
     * @param teacherId 教师id
     * @return
     */
    @Modifying
    @Transactional
    @Query(value = "update CourseLive cl set cl.assistantId = ?2 ,cl.assConfirm=?3 where cl.id = ?1")
    int updateAssistant(Long liveId, Long teacherId,CourseConfirmStatus confirm);

    /**
     * 修改直播绑定学习师
     * @param liveId 直播id
     * @param teacherId 教师id
     * @return
     */
    @Modifying
    @Transactional
    @Query(value = "update CourseLive cl set cl.learningTeacherId = ?2 ,cl.ltConfirm=?3 where cl.id = ?1")
    int updateLearningTeacher(Long liveId, Long teacherId,CourseConfirmStatus confirm);

    /**
     * 修改直播绑定场控
     * @param liveId 直播id
     * @param teacherId 教师id
     * @return
     */
    @Modifying
    @Transactional
    @Query(value = "update CourseLive cl set cl.controllerId = ?2 ,cl.ctrlConfirm=?3 where cl.id = ?1")
    int updateController(Long liveId, Long teacherId,CourseConfirmStatus confirm);

    /**
     * 修改直播绑定主持人
     * @param liveId 直播id
     * @param teacherId 教师id
     * @return
     */
    @Modifying
    @Transactional
    @Query(value = "update CourseLive cl set cl.compereId = ?2 ,cl.comConfirm=?3 where cl.id = ?1")
    int updateCompere(Long liveId, Long teacherId,CourseConfirmStatus confirm);

    /**
	 * 根据课程ID和日期查询直播
	 * 
	 * @param courseId
	 *            课程ID
	 * @param dates
	 *            日期
	 * @return 课程直播
	 */
	List<CourseLive> findByCourseIdAndDateIn(Long courseId, List<Date> dates);

    /**
     * 修改直播名称
     * @param liveId
     * @param liveName
     * @return
     */
    @Modifying
    @Transactional
    @Query(value = "update CourseLive cl set cl.name = ?2 where cl.id = ?1")
    int updateLiveName(Long liveId, String liveName);

    /**
     * 根据课程直播id查找课程状态
     * @param liveId 课程直播id
     * @return 课程状态
     */
    @Query(value = "SELECT c.status FROM  course_live cl  INNER JOIN course c ON c.id = cl.course_id WHERE cl.id = ?1",nativeQuery = true)
    CourseStatus findCourseStatusByCourseLiveId(Long liveId);

    /**
	 * 根据课程ID日期和时间查询直播
	 * 
	 * @param courseId
	 *            课程ID
	 * @param dates
	 *            日期
	 * @param timeBegin
	 *            开始时间
	 * @param timeEnd
	 *            结束时间
	 * @return 课程直播
	 */
	List<CourseLive> findByCourseIdAndDateInAndTimeBeginAndTimeEnd(Long courseId, List<Date> dates, Integer timeBegin,
			Integer timeEnd);

//    /**
//     * 根据日期和教师id查询直播
//     * @param dateBegin 开始日期
//     * @param dateEnd 结束日期
//     * @param teacherId 教师id
//     * @param status 课程状态
//     * @return 直播集合
//     */
//	@Query(value = "SELECT cl.* from course_live cl LEFT JOIN course c ON c.id = cl.course_id LEFT JOIN course_live_teacher clt ON cl.id=clt.course_live_id " +
//            "WHERE cl.date between ?1 and ?2 and c.status = ?4  and clt.teacher_id=?3 " +
//            "ORDER BY cl.date_int desc,cl.time_begin desc , cl.time_end desc",nativeQuery = true)
//    List<CourseLive> findByDateAndTeacherId(Date dateBegin, Date dateEnd, Long teacherId,int status);

    /**
     * 修改直播面试授课类型
     * @param liveId 直播id
     * @param courseLiveCategory 授课类型
     */
    @Modifying
    @Transactional
    @Query(value = "update CourseLive cl set cl.courseLiveCategory = ?2 where cl.id = ?1")
    int updateCourseLiveCategoryByStatus(Long liveId, CourseLiveCategory courseLiveCategory);

    @Query(value = "SELECT * FROM course_live cl LEFT JOIN course c ON c.id = cl.course_id WHERE cl.ass_confirm >= 2 AND c.assistant_flag = TRUE AND c.status >=4",nativeQuery = true)
    List<CourseLive> getTaskDGTByAss();

    @Query(value = "SELECT * FROM course_live cl LEFT JOIN course c ON c.id = cl.course_id WHERE cl.com_confirm >= 2 AND c.compere_flag = TRUE AND c.status >=4",nativeQuery = true)
    List<CourseLive> getTaskDGTByCom();

    @Query(value = "SELECT * FROM course_live cl LEFT JOIN course c ON c.id = cl.course_id WHERE cl.ctrl_confirm >= 2 AND c.controller_flag = TRUE AND c.status >=4",nativeQuery = true)
    List<CourseLive> getTaskDGTByCtrl();

    @Query(value = "SELECT * FROM course_live cl LEFT JOIN course c ON c.id = cl.course_id WHERE cl.lt_confirm >= 2 AND c.learning_teacher_flag = TRUE AND c.status >=4",nativeQuery = true)
    List<CourseLive> getTaskDGTByLt();


}
