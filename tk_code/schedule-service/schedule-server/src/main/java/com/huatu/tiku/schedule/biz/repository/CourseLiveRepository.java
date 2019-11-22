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

//    /**
//     * 修改直播绑定直播间
//     *
//     * @param roomId 直播间id
//     * @param liveId 直播id
//     * @return
//     */
//    @Modifying
//    @Transactional
//    @Query(value = "update CourseLive cl set cl.liveRoomId = ?1 where cl.id = ?2")
//    int updateLiveRoom(Long roomId, Long liveId);

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
    @Query(value = "update CourseLive cl set cl.name = ?2 where cl.id = ?1 or cl.sourceId=?1")
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
    @Query(value = "update CourseLive cl set cl.courseLiveCategory = ?2 where cl.id = ?1 or cl.sourceId=?1")
    int updateCourseLiveCategoryByStatus(Long liveId, CourseLiveCategory courseLiveCategory);

    List<CourseLive> findByVideoRoomId(Long roomId);

    @Query(value = "select id from  CourseLive  where sourceId in ?1 ")
    List<Long> findBySourceIdIn(List<Long> ids);

    @Query(value = "select * from  course_live  where id=?1 or  source_id = ?1 ",nativeQuery = true)
    List<CourseLive> findByIdOrSourceId(Long id);

    @Query(value = "select * from  course_live  where  source_id = ?1 ",nativeQuery = true)
    List<CourseLive> findSourceId(Long id);

}
