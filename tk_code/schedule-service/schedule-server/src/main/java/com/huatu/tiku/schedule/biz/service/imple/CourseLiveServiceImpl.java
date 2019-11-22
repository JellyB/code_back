package com.huatu.tiku.schedule.biz.service.imple;

import java.util.*;
import java.util.stream.Collectors;

import javax.persistence.criteria.*;

import com.huatu.tiku.schedule.biz.constant.SMSTemplate;
import com.huatu.tiku.schedule.biz.domain.*;
import com.huatu.tiku.schedule.biz.dto.CreateCourseVideoDto;
import com.huatu.tiku.schedule.biz.dto.UpdateCourseVideoDto;
import com.huatu.tiku.schedule.biz.dto.UpdateDateTimeBatchDto;
import com.huatu.tiku.schedule.biz.dto.UpdateDateTimeDto;
import com.huatu.tiku.schedule.biz.enums.*;
import com.huatu.tiku.schedule.biz.repository.*;
import com.huatu.tiku.schedule.biz.util.*;
import com.huatu.tiku.schedule.biz.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.schedule.base.exception.BadRequestException;
import com.huatu.tiku.schedule.base.service.impl.BaseServiceImpl;
import com.huatu.tiku.schedule.biz.bean.TeacherScoreBean;
import com.huatu.tiku.schedule.biz.service.CourseLiveService;
import com.huatu.tiku.schedule.biz.service.TeacherService;

@Service
@Slf4j
public class CourseLiveServiceImpl extends BaseServiceImpl<CourseLive, Long> implements CourseLiveService {

    @Autowired
    private CourseLiveRepository courseLiveRepository;

    @Autowired
    private CourseLiveTeacherRepository courseLiveTeacherRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private ConfirmTokenRepository confirmTokenRepository;

    @Value("${api.host}")
    private String apiHost;

    @Autowired
    private TeacherSubjectRepository teacherSubjectRepository;

    @Override
    @Transactional
    public List<CourseLive>  createCourseLive(Long courseId, Long subjectId, List<Date> dates, List<List<String>> times, Boolean token) {
        List<CourseLive> courseLives = Lists.newArrayList();
        if (null != token && token) {
            Date begin = dates.get(0);//开始日期
            Date end = dates.get(1);//结束日期
            Calendar dateBegin = Calendar.getInstance();
            dateBegin.setTime(begin);
            dates.clear();
            while (dateBegin.getTime().getTime() <= end.getTime()) {
                dates.add(dateBegin.getTime());
                dateBegin.add(Calendar.DAY_OF_YEAR, 1);
            }
        }
        Course course = courseRepository.findOne(courseId);
        dates.forEach(date -> times.forEach(time -> {
            Integer timeBegin = Integer.parseInt(time.get(0).replace(":", ""));
            Integer timeEnd = Integer.parseInt(time.get(1).replace(":", ""));

            // 判断直播是否已经存在
//            Boolean existsFlag = courseLiveRepository.existsByCourseIdAndDateAndTimeBeginAndTimeEnd(
//                    courseId, date, timeBegin, timeEnd);
//
//            // 如果不存在创建
//            if (!existsFlag) {
                // 创建直播
                CourseLive courseLive = new CourseLive();
                courseLive.setCourseId(courseId);
//					if(ExamType.MS.equals(course.getExamType())&&CourseCategory.XXK.equals(course.getCourseCategory())){//面试类型 且为线下课
//                if (ExamType.MS.equals(course.getExamType())) {//面试类型
                    courseLive.setCourseLiveCategory(CourseLiveCategory.SK);//默认授课类型
//                }
                courseLive.setTimeBegin(timeBegin);
                courseLive.setTimeEnd(timeEnd);
                courseLive.setDate(date);
                courseLive.setDateInt(Integer.parseInt(DateformatUtil.format1(date)));

                courseLiveRepository.save(courseLive);

                // 创建直播教师
                CourseLiveTeacher courseLiveTeacher = new CourseLiveTeacher();
                courseLiveTeacher.setCourseLiveId(courseLive.getId());
                courseLiveTeacher.setConfirm(CourseConfirmStatus.DQR);
                courseLiveTeacher.setSubjectId(subjectId);
                courseLiveTeacher.setTeacherCourseLevel(TeacherCourseLevel.GOOD);//默认专长级别
                courseLiveTeacher.setTeacherType(TeacherType.JS);//默认教师类型
                courseLiveTeacherRepository.save(courseLiveTeacher);

                // 返回课程直播教师
                courseLive.setCourseLiveTeachers(Collections.singletonList(courseLiveTeacher));

                courseLives.add(courseLive);
//            }
        }));

        return courseLives;
    }

    @Override
    public Boolean existsByCourseIdAndDateAndTimeBeginAndTimeEnd(Long courseId, Date date, Integer timeBegin,
                                                                 Integer timeEnd) {
        return courseLiveRepository.existsByCourseIdAndDateAndTimeBeginAndTimeEnd(courseId, date, timeBegin, timeEnd);
    }

    @Override
    public CourseLive findOneByCourseIdAndDateAndTimeBeginAndTimeEnd(Long courseId, Date date, Integer timeBegin,
                                                                     Integer timeEnd) {
        return courseLiveRepository.findOneByCourseIdAndDateAndTimeBeginAndTimeEnd(courseId, date, timeBegin, timeEnd);
    }

    /**
     * 根据教师类型 相应寻找
     * @param teacher 教师
     */
    @Override
    public List<TaskLiveVo> findTaskTeacher(Teacher teacher) {
        List<CourseLive> courseLivesDQR ;
        List<CourseLive> courseLivesDGT ;
        Long teacherId = teacher.getId();
        courseLivesDQR = courseLiveRepository.findTaskTeacherByTeacherId(teacherId, CourseConfirmStatus.DQR.ordinal(), CourseStatus.JSQR.ordinal());
        courseLivesDGT = courseLiveRepository.findTaskTeacherByTeacherId(teacherId, CourseConfirmStatus.DGT.ordinal(), CourseStatus.JSQR.ordinal());
        List<TaskLiveVo> taskLiveVos = new ArrayList<>();
        courseLivesDQR.forEach(taskLive -> {//封装结果集
            TaskLiveVo taskLiveVo = new TaskLiveVo();
            taskLiveVo.setLiveId(taskLive.getId());
            taskLiveVo.setLiveName(taskLive.getName());
            taskLiveVo.setCourseId(taskLive.getCourseId());
            taskLiveVo.setCourseName(taskLive.getCourse().getName());
            Integer begin = taskLive.getTimeBegin();
            Integer end = taskLive.getTimeEnd();
            if (null != begin && null != end) {
                String timeBegin = TimeRangeUtil.intToDateString(begin);
                String timeEnd = TimeRangeUtil.intToDateString(end);
                taskLiveVo.setTimeRange(taskLive.getDate().toString() + " " + timeBegin + "-" + timeEnd);
            }
            taskLiveVo.setConfirmKey(CourseConfirmStatus.DQR.getValue());
            taskLiveVo.setConfirmStatus(CourseConfirmStatus.DQR.getText());
            taskLiveVo.setDateInt(taskLive.getDateInt());
            taskLiveVo.setTimeBegin(taskLive.getTimeBegin());
            taskLiveVo.setTimeEnd(taskLive.getTimeEnd());
            taskLiveVos.add(taskLiveVo);
        });
        courseLivesDGT.forEach(taskLive -> {//封装结果集
            TaskLiveVo taskLiveVo = new TaskLiveVo();
            taskLiveVo.setLiveId(taskLive.getId());
            taskLiveVo.setLiveName(taskLive.getName());
            taskLiveVo.setCourseId(taskLive.getCourseId());
            taskLiveVo.setCourseName(taskLive.getCourse().getName());
            Integer begin = taskLive.getTimeBegin();
            Integer end = taskLive.getTimeEnd();
            if (null != begin && null != end) {
                String timeBegin = TimeRangeUtil.intToDateString(begin);
                String timeEnd = TimeRangeUtil.intToDateString(end);
                taskLiveVo.setTimeRange(taskLive.getDate().toString() + " " + timeBegin + "-" + timeEnd);
            }
            taskLiveVo.setConfirmKey(CourseConfirmStatus.DGT.getValue());
            taskLiveVo.setConfirmStatus(CourseConfirmStatus.DGT.getText());
            taskLiveVo.setDateInt(taskLive.getDateInt());
            taskLiveVo.setTimeBegin(taskLive.getTimeBegin());
            taskLiveVo.setTimeEnd(taskLive.getTimeEnd());
            taskLiveVos.add(taskLiveVo);
        });
        taskLiveVos.sort((o1,o2)->{
            int result=o1.getDateInt()-o2.getDateInt();//日期排序
            result=result==0?o1.getTimeBegin()-o2.getTimeBegin():result;//开始时间排序
            result=result==0?o1.getTimeEnd()-o2.getTimeEnd():result;//结束时间排序
            return result;
        });
        return taskLiveVos;
    }


    @Override
    public Page<CourseLiveScheduleVo> schedule(List<ExamType> examTypes, Long subjectId, Date dateBegin, Date dateEnd,
                                               Long teacherId, Pageable page, List<CourseCategory> categorys) {
        List<Sort.Order> list = Lists.newArrayList();
        list.add(new Sort.Order(Sort.Direction.ASC, "dateInt"));//日期
        list.add(new Sort.Order(Sort.Direction.ASC, "timeBegin"));//开始时间
        list.add(new Sort.Order(Sort.Direction.ASC, "timeEnd"));//结束时间
        Pageable pageable = new PageRequest(page.getPageNumber(), page.getPageSize(), new Sort(list));
        Page<CourseLive> pages = courseLiveRepository.findAll((root, criteriaQuery, criteriaBuilder) -> {
            criteriaQuery.distinct(true);

//				criteriaQuery.orderBy(new OrderImpl(root.get("timeBegin")));

            List<Predicate> predicates = new ArrayList<>();

            Join<CourseLive, Course> course = root.join("course");
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(course.get("status"), CourseStatus.JSQR));
            if(null!=categorys&&!categorys.isEmpty()){
                predicates.add(course.get("courseCategory").in(categorys));
            }

            if (dateBegin != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("date"), dateBegin));
            }

            if (dateEnd != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("date"), dateEnd));
            }

            Join<CourseLive, CourseLiveTeacher> courseLiveTeachers = root.join("courseLiveTeachers");

            if (teacherId != null) {
                predicates.add(criteriaBuilder.equal(courseLiveTeachers.get("teacherId"), teacherId));
            }
            predicates.add(criteriaBuilder.equal(courseLiveTeachers.get("confirm"), CourseConfirmStatus.QR));//添加确认条件

            Join<CourseLiveTeacher, Teacher> teachers=courseLiveTeachers.join("teacher");
            if (examTypes != null ) {
                if(examTypes.isEmpty()){
                    predicates.add(criteriaBuilder.equal(root.get("examType"), ExamType.ALL));
                }else{
                    predicates.add(teachers.get("examType").in(examTypes));
                }
            }
            if (subjectId != null) {
                predicates.add(criteriaBuilder.equal(teachers.get("subjectId"), subjectId));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        }, pageable);
        List<CourseLive> courseLives = pages.getContent();
        // 直播
        List<CourseLiveScheduleVo> courseLiveScheduleVos = Lists.newArrayList();

        // 直播字典
        Map<Long, CourseLiveScheduleVo> courseLiveDic = Maps.newHashMap();

        courseLives.forEach(courseLive -> {
            CourseLiveScheduleVo courseLiveScheduleVo = new CourseLiveScheduleVo();

            courseLiveScheduleVo.setId(courseLive.getId());
            courseLiveScheduleVo.setDate(
                    DateformatUtil.format2(courseLive.getDate()) + " " + DateformatUtil.format4(courseLive.getDate()));
            courseLiveScheduleVo.setTimeBegin(TimeformatUtil.format(courseLive.getTimeBegin()));
            courseLiveScheduleVo.setTimeEnd(TimeformatUtil.format(courseLive.getTimeEnd()));
            courseLiveScheduleVo.setCourseName(courseLive.getCourse().getName());
            courseLiveScheduleVo.setCategoryName(courseLive.getCourse().getCourseCategory().getText());

            List<CourseLiveScheduleVo.TeacherInfo> teacherInfos = Lists.newArrayList();

            courseLive.getCourseLiveTeachers().forEach(courseLiveTeacher -> {
                if(CourseConfirmStatus.QR.equals(courseLiveTeacher.getConfirm())) {
                    CourseLiveScheduleVo.TeacherInfo teacherInfo = new CourseLiveScheduleVo.TeacherInfo();

                    if (courseLiveTeacher.getTeacherType() != null) {
                        teacherInfo.setType(courseLiveTeacher.getTeacherType());
                    }

                    if (courseLiveTeacher.getSubject() != null) {
                        teacherInfo.setSubject(courseLiveTeacher.getSubject().getName());
                    }

                    if (courseLiveTeacher.getTeacher() != null) {
                        teacherInfo.setName(courseLiveTeacher.getTeacher().getName());
                        teacherInfo.setTeacherId(courseLiveTeacher.getTeacherId());

                        teacherInfos.add(teacherInfo);
                    }
                }
            });

            courseLiveScheduleVo.setTeacherInfos(teacherInfos);

            courseLiveScheduleVo.setCourseLiveName(courseLive.getName());

            courseLiveScheduleVo.setExamType(courseLive.getCourse().getExamType().getText());

            Subject subject = courseLive.getCourse().getSubject();
            if (subject != null) {
                courseLiveScheduleVo.setSubject(subject.getName());
            }

            courseLiveScheduleVo.setSourceId(courseLive.getSourceId());
            VideoRoom videoRoom = courseLive.getVideoRoom();
            if(null!=videoRoom){
                courseLiveScheduleVo.setVideoRoomId(videoRoom.getId());
                courseLiveScheduleVo.setVideoRoomName(videoRoom.getName());
            }
            courseLiveScheduleVos.add(courseLiveScheduleVo);

            courseLiveDic.put(courseLive.getId(), courseLiveScheduleVo);
        });

        // 处理滚动排课
        Iterator<CourseLiveScheduleVo> courseLiveScheduleVoIterator = courseLiveScheduleVos.iterator();
        while (courseLiveScheduleVoIterator.hasNext()) {
            CourseLiveScheduleVo temp = courseLiveScheduleVoIterator.next();
            // 如果是滚动排课，则只在源课程中追加课程名称
            if (temp.getSourceId() != null) {
                CourseLiveScheduleVo sourceCourseLive = courseLiveDic.get(temp.getSourceId());
                if (sourceCourseLive != null) {//元课程在集合当中
                    sourceCourseLive.setCourseName(sourceCourseLive.getCourseName() + "，" + temp.getCourseName());

                    courseLiveScheduleVoIterator.remove();
                }else{  //集合中没有源数据
                    CourseLive sourceLive = courseLiveRepository.findOne(temp.getSourceId());//查找源数据
                    CourseLiveScheduleVo courseLiveScheduleVo = new CourseLiveScheduleVo();

                    courseLiveScheduleVo.setId(sourceLive.getId());
                    courseLiveScheduleVo.setDate(
                            DateformatUtil.format2(sourceLive.getDate()) + " " + DateformatUtil.format4(sourceLive.getDate()));
                    courseLiveScheduleVo.setTimeBegin(TimeformatUtil.format(sourceLive.getTimeBegin()));
                    courseLiveScheduleVo.setTimeEnd(TimeformatUtil.format(sourceLive.getTimeEnd()));
                    courseLiveScheduleVo.setCourseName(sourceLive.getCourse().getName());
                    courseLiveScheduleVo.setCategoryName(sourceLive.getCourse().getCourseCategory().getText());

                    List<CourseLiveScheduleVo.TeacherInfo> teacherInfos = Lists.newArrayList();

                    sourceLive.getCourseLiveTeachers().forEach(courseLiveTeacher -> {
                        if(CourseConfirmStatus.QR.equals(courseLiveTeacher.getConfirm())) {
                            CourseLiveScheduleVo.TeacherInfo teacherInfo = new CourseLiveScheduleVo.TeacherInfo();

                            if (courseLiveTeacher.getTeacherType() != null) {
                                teacherInfo.setType(courseLiveTeacher.getTeacherType());
                            }

                            if (courseLiveTeacher.getSubject() != null) {
                                teacherInfo.setSubject(courseLiveTeacher.getSubject().getName());
                            }

                            if (courseLiveTeacher.getTeacher() != null) {
                                teacherInfo.setName(courseLiveTeacher.getTeacher().getName());
                                teacherInfo.setTeacherId(courseLiveTeacher.getTeacherId());

                                teacherInfos.add(teacherInfo);
                            }
                        }
                    });

                    courseLiveScheduleVo.setTeacherInfos(teacherInfos);
                    courseLiveScheduleVo.setCourseLiveName(sourceLive.getName());
                    courseLiveScheduleVo.setExamType(sourceLive.getCourse().getExamType().getText());
                    Subject subject = sourceLive.getCourse().getSubject();
                    if (subject != null) {
                        courseLiveScheduleVo.setSubject(subject.getName());
                    }
                    courseLiveScheduleVo.setSourceId(sourceLive.getSourceId());
                    VideoRoom videoRoom = sourceLive.getVideoRoom();
                    if(null!=videoRoom){
                        courseLiveScheduleVo.setVideoRoomId(videoRoom.getId());
                        courseLiveScheduleVo.setVideoRoomName(videoRoom.getName());
                    }
                    courseLiveDic.put(sourceLive.getId(), courseLiveScheduleVo);//存储元数据

                    sourceCourseLive=courseLiveScheduleVo;
                    temp.setCourseName(sourceCourseLive.getCourseName() + "，" + temp.getCourseName());//拼接名称

                }
            }
        }
        return new PageImpl(courseLiveScheduleVos, page, courseLiveScheduleVos == null ? 0 : pages.getTotalElements());
    }

//    @Override
//    public int updateLiveRoom(Long liveId, Long roomId) {
//        return courseLiveRepository.updateLiveRoom(roomId, liveId);
//    }

    @Override
    public List<CourseLiveRollingVo> findForRolling(Long currentCourseId, List<Date> dates, Long courseId,
                                                    ExamType examType, Long subjectId) {
        List<Sort.Order> list = Lists.newArrayList();
        list.add(new Sort.Order(Sort.Direction.ASC, "dateInt"));//日期
        list.add(new Sort.Order(Sort.Direction.ASC, "timeBegin"));//开始时间
        list.add(new Sort.Order(Sort.Direction.ASC, "timeEnd"));//开始时间
        Sort sort = new Sort(list);
        List<CourseLive> courseLives = courseLiveRepository.findAll((root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            Join<CourseLive, Course> course = root.join("course");

            predicates.add(criteriaBuilder.notEqual(course.get("id"), currentCourseId));

            predicates.add(criteriaBuilder.equal(course.get("status"), CourseStatus.WC));

            predicates.add(criteriaBuilder.isNull(root.get("sourceId")));//不为滚动排课
//                predicates.add(criteriaBuilder.equal(course.get("courseCategory"), CourseCategory.LIVE));//直播课程

            if (dates != null && !dates.isEmpty()) {
                predicates.add(root.get("date").in(dates));
            }

            if (examType != null) {
                predicates.add(criteriaBuilder.equal(course.get("examType"), examType));
            }

            if (subjectId != null) {
                predicates.add(criteriaBuilder.equal(course.get("subjectId"), subjectId));
            }

            if (courseId != null) {
                predicates.add(criteriaBuilder.equal(root.get("courseId"), courseId));
            }

            predicates.add(criteriaBuilder.notEqual(course.get("courseCategory"), CourseCategory.VIDEO));

            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        },sort);

        List<CourseLiveRollingVo> courseLiveRollingVos = Lists.newArrayList();

        courseLives.forEach(courseLive -> {
            CourseLiveRollingVo courseLiveRollingVo = new CourseLiveRollingVo();
            courseLiveRollingVo.setId(courseLive.getId());
            courseLiveRollingVo.setCourseName(courseLive.getCourse().getName());
            courseLiveRollingVo.setCourseLiveName(courseLive.getName());

            Subject subject = courseLive.getCourse().getSubject();

            if (subject != null) {
                courseLiveRollingVo.setSubject(subject.getName());
            }

            courseLiveRollingVo.setDate(DateformatUtil.format2(courseLive.getDate()));
            courseLiveRollingVo.setTimeBegin(TimeformatUtil.format(courseLive.getTimeBegin()));
            courseLiveRollingVo.setTimeEnd(TimeformatUtil.format(courseLive.getTimeEnd()));

            courseLiveRollingVos.add(courseLiveRollingVo);
        });

        return courseLiveRollingVos;
    }

    @Override
    @Transactional
    public void rollingSchedule(Long courseId, List<Long> courseLiveIds) {
        // 获取当前课程
        Course course = courseRepository.findOne(courseId);
        if (course == null) {
            throw new BadRequestException("课程不存在");
        }

        // 获取课程直播
        List<CourseLive> courseLives = courseLiveRepository.findAll(courseLiveIds);
        if (courseLiveIds.isEmpty()) {
            throw new BadRequestException("课程直播不存在");
        }

        // 判断课程是否已包含直播
        courseLives.forEach(courseLive -> {
            // 滚动排课源
            Long sourceId = courseLive.getId();

            // 获取课程原有直播
            CourseLive courseLiveTemp = courseLiveRepository.findOneByCourseIdAndDateAndTimeBeginAndTimeEnd(courseId,
                    courseLive.getDate(), courseLive.getTimeBegin(), courseLive.getTimeEnd());
            if (courseLiveTemp == null) {
                // 不存在，直接创建
                courseLiveTemp = new CourseLive();
            } else {
                // 已经存在，判断是否已经关联删除课程直播教师
                courseLiveTeacherRepository.delete(courseLiveTemp.getCourseLiveTeachers());
                courseLiveTemp.setCourseLiveTeachers(null);
            }

            BeanUtils.copyProperties(courseLive, courseLiveTemp, "id", "course", "liveRoom", "assistant", "controller",
                    "compere", "courseLiveTeachers");

            courseLiveTemp.setCourseId(course.getId());
            courseLiveTemp.setSourceId(sourceId);

            courseLiveRepository.save(courseLiveTemp);

            // 复制courseLiveTeachers
            List<CourseLiveTeacher> courseLiveTeachers = courseLive.getCourseLiveTeachers();

            Long courseLiveId = courseLiveTemp.getId();

            List<CourseLiveTeacher> courseliveteachersTemp = Lists.newArrayList();

            courseLiveTeachers.forEach(courseLiveTeacher -> {
                CourseLiveTeacher courseLiveTeacherTemp = new CourseLiveTeacher();

                BeanUtils.copyProperties(courseLiveTeacher, courseLiveTeacherTemp, "id", "courseLive", "module",
                        "teacher");

                courseLiveTeacherTemp.setCourseLiveId(courseLiveId);
                courseLiveTeacherTemp.setSourceId(courseLiveTeacher.getId());

                courseliveteachersTemp.add(courseLiveTeacherTemp);
            });

            courseLiveTeacherRepository.save(courseliveteachersTemp);
        });
    }


    @Override
    public List<CourseLive> findByCourseIdAndDateIn(Long courseId, List<Date> dates) {
        return courseLiveRepository.findByCourseIdAndDateIn(courseId, dates);
    }

    @Override
    public int updateLiveName(Long liveId, String liveName) {
        return courseLiveRepository.updateLiveName(liveId, liveName);
    }

    @Override
    public void oneKeySchedule(List<Long> courseLiveIds) {
        // 获取直播
        List<CourseLive> courseLives;
        if(null!=courseLiveIds&&!courseLiveIds.isEmpty()){
            courseLives= courseLiveRepository.findAll(courseLiveIds);
        }else{
            return;
        }

        // 课程
        Course course = courseLives.get(0).getCourse();
        CourseStatus status = course.getStatus();
        if (status != null && status.equals(CourseStatus.ZBAP)) {//如果直播安排状态 表示已经被运营撤销
            throw new BadRequestException("该课程已撤销,请勿操作");
        }
        // 遍历课程直播
        for (CourseLive courseLive : courseLives) {

            if(null!=courseLive.getSourceId()){
                continue ;

            }
            courseLive.getCourseLiveTeachers().forEach(courseLiveTeacher -> {
                // 指定教师
                if (courseLiveTeacher.getTeacherId() == null&&TeacherType.JS.equals(courseLiveTeacher.getTeacherType())) {//针对教师数据
                    List<TeacherScoreBean> teachers = teacherService.autoGetAvailableTeachers(courseLive.getDate(),
                            courseLive.getTimeBegin(), courseLive.getTimeEnd(), course.getExamType(),
                            courseLiveTeacher.getSubjectId(), courseLiveTeacher.getTeacherCourseLevel(),
                            course.getId(), true);//筛选高等级教师
                    if (teachers.size() > 0) {
//                        courseLiveTeacher.setTeacherId(teachers.get(0).getId());
//                        courseLiveTeacherRepository.save(courseLiveTeacher);
                        courseLiveTeacherRepository.bindTeacher(courseLiveTeacher.getId(),teachers.get(0).getId(),CourseConfirmStatus.DQR.ordinal());
                    }
                }
            });
        }
    }

    @Override
    public void oneKeyScheduleAssistant( List<Long> courseLiveIds) {
        // 获取直播
        List<CourseLive> courseLives;
        if(null!=courseLiveIds&&!courseLiveIds.isEmpty()){
            courseLives= courseLiveRepository.findAll(courseLiveIds);
        }else{
            return;
        }

        // 课程
        Course course = courseLives.get(0).getCourse();

        // 遍历课程直播
        for (CourseLive courseLive : courseLives) {

            courseLive.getCourseLiveTeachers().forEach(courseLiveTeacher -> {
                // 指定教师
                if (courseLiveTeacher.getTeacherId() == null&&TeacherType.ZJ.equals(courseLiveTeacher.getTeacherType())) {  //教师为空 且为助教类型
                    List<TeacherScoreBean> teachers = teacherService.autoGetAvailableTeachers(courseLive.getDate(),
                            courseLive.getTimeBegin(), courseLive.getTimeEnd(), course.getExamType(),
                            courseLiveTeacher.getSubjectId(), courseLiveTeacher.getTeacherCourseLevel(),
                            course.getId(),false);//筛选对应等级
                    if (teachers.size() > 0) {
//                        courseLiveTeacher.setTeacherId(teachers.get(0).getId());
//                        courseLiveTeacherRepository.save(courseLiveTeacher);
                        courseLiveTeacherRepository.bindTeacher(courseLiveTeacher.getId(),teachers.get(0).getId(),CourseConfirmStatus.DQR.ordinal());
                    }
                }
                if (courseLiveTeacher.getTeacherId() == null&&TeacherType.ZCR.equals(courseLiveTeacher.getTeacherType())) {  //教师为空 主持人类型
                    List<TeacherScoreBean> teachers = teacherService.getAvailableCtrl(courseLive.getDate(),
                            courseLive.getTimeBegin(), courseLive.getTimeEnd(),TeacherType.ZCR);//筛选对应等级
                    if (teachers.size() > 0) {
//                        courseLiveTeacher.setTeacherId(teachers.get(0).getId());
//                        courseLiveTeacherRepository.save(courseLiveTeacher);
                        courseLiveTeacherRepository.bindTeacher(courseLiveTeacher.getId(),teachers.get(0).getId(),CourseConfirmStatus.DQR.ordinal());
                    }
                }
                if (courseLiveTeacher.getTeacherId() == null&&TeacherType.CK.equals(courseLiveTeacher.getTeacherType())) {  //教师为空 场控类型
                    List<TeacherScoreBean> teachers = teacherService.getAvailableCtrl(courseLive.getDate(),
                            courseLive.getTimeBegin(), courseLive.getTimeEnd(),TeacherType.CK );//筛选对应等级
                    if (teachers.size() > 0) {
//                        courseLiveTeacher.setTeacherId(teachers.get(0).getId());
//                        courseLiveTeacherRepository.save(courseLiveTeacher);
                        courseLiveTeacherRepository.bindTeacher(courseLiveTeacher.getId(),teachers.get(0).getId(),CourseConfirmStatus.DQR.ordinal());
                    }
                }
            });
        }
    }

    @Override
    public void findCourseStatusByCourseLiveId(Long liveId) {
        CourseStatus status = courseLiveRepository.findCourseStatusByCourseLiveId(liveId);
        if (status != null && status.equals(CourseStatus.ZBAP)) {//如果直播安排状态 表示已经被运营撤销
            throw new BadRequestException("该课程已撤销,请勿操作");
        }
    }

    @Override
    public List<CourseLiveScheduleVo> mySchedule(ExamType examType, Long subjectId, Date dateBegin, Date dateEnd,
                                                 String courseName, Long liveRoomId, Long teacherId) {
        List<CourseLive> courseLives = courseLiveRepository.findAll((root, criteriaQuery, criteriaBuilder) -> {
            criteriaQuery.distinct(true);

            List<Predicate> predicates = new ArrayList<>();

            Join<CourseLive, Course> course = root.join("course");
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(course.get("status"), CourseStatus.ZJAP));
            if (examType != null) {
                predicates.add(criteriaBuilder.equal(course.get("examType"), examType));
            }

            if (subjectId != null) {
                predicates.add(criteriaBuilder.equal(course.get("subjectId"), subjectId));
            }

            if (dateBegin != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("date"), dateBegin));
            }

            if (dateEnd != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("date"), dateEnd));
            }

            Join<CourseLive, CourseLiveTeacher> courseLiveTeachers = root.join("courseLiveTeachers");
            predicates.add(criteriaBuilder.equal(courseLiveTeachers.get("confirm"), CourseConfirmStatus.QR));
            if (teacherId != null) {
                predicates.add(criteriaBuilder.equal(courseLiveTeachers.get("teacherId"), teacherId));
            }

            if (!Strings.isNullOrEmpty(courseName)) {
                predicates.add(criteriaBuilder.like(course.get("name"), "%" + courseName + "%"));
            }

            if (liveRoomId != null) {
                predicates.add(criteriaBuilder.equal(root.get("liveRoomId"), liveRoomId));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        });
        courseLives.sort((o1,o2)->{
            int result=o1.getDateInt()-o2.getDateInt();//日期排序
            result=result==0?o1.getTimeBegin()-o2.getTimeBegin():result;//开始时间排序
            result=result==0?o1.getTimeEnd()-o2.getTimeEnd():result;//结束时间排序
            return result;
        });
        // 直播
        List<CourseLiveScheduleVo> courseLiveScheduleVos = Lists.newArrayList();

        // 直播字典
        Map<Long, CourseLiveScheduleVo> courseLiveDic = Maps.newHashMap();

        courseLives.forEach(courseLive -> {
            CourseLiveScheduleVo courseLiveScheduleVo = new CourseLiveScheduleVo();

            courseLiveScheduleVo.setId(courseLive.getId());
            courseLiveScheduleVo.setDate(
                    DateformatUtil.format0(courseLive.getDate()));
            courseLiveScheduleVo.setTimeBegin(TimeformatUtil.format(courseLive.getTimeBegin()));
            courseLiveScheduleVo.setTimeEnd(TimeformatUtil.format(courseLive.getTimeEnd()));
            courseLiveScheduleVo.setCourseName(courseLive.getCourse().getName());
            courseLiveScheduleVo.setCategoryName(courseLive.getCourse().getCourseCategory().getText());
            courseLiveScheduleVo.setPlace(courseLive.getCourse().getPlace());
            VideoRoom videoRoom = courseLive.getVideoRoom();
            if(null!=videoRoom){
                courseLiveScheduleVo.setVideoRoomName(videoRoom.getName());
                courseLiveScheduleVo.setVideoRoomId(videoRoom.getId());
            }
            StringBuffer sbTeacher = new StringBuffer();
            StringBuffer sbAss = new StringBuffer();
            StringBuffer sbCom = new StringBuffer();
            StringBuffer sbCtrl = new StringBuffer();
            StringBuffer sbSys = new StringBuffer();
            StringBuffer sbZks = new StringBuffer();
            courseLive.getCourseLiveTeachers().forEach(courseLiveTeacher -> {
                if (courseLiveTeacher.getTeacherId()!=null&&CourseConfirmStatus.QR.equals(courseLiveTeacher.getConfirm())) {  //选择了教师 且确认了的教师
                    if(courseLiveTeacher.getTeacherId().equals(teacherId)){  //跟指定教师关联的数据
                        Subject subject =courseLiveTeacher.getSubject();  //写入科目数据
                        if (subject != null) {
                            courseLiveScheduleVo.setSubject(subject.getName());
                        }
                    }
                    String name;
                    switch (courseLiveTeacher.getTeacherType()){
                        case JS:
                            name = courseLiveTeacher.getTeacher().getName();
                            sbTeacher.append(name);
                            sbTeacher.append(",");
                            break;
                        case ZCR:
                            name = courseLiveTeacher.getTeacher().getName();
                            sbCom.append(name);
                            sbCom.append(",");
                            break;
                        case ZJ:
                            name = courseLiveTeacher.getTeacher().getName();
                            sbAss.append(name);
                            sbAss.append(",");
                            break;
                        case CK:
                            name = courseLiveTeacher.getTeacher().getName();
                            sbCtrl.append(name);
                            sbCtrl.append(",");
                            break;
                        case SYS:
                            name = courseLiveTeacher.getTeacher().getName();
                            sbSys.append(name);
                            sbSys.append(",");
                            break;
                        case ZKS:
                            name = courseLiveTeacher.getTeacher().getName();
                            sbZks.append(name);
                            sbZks.append(",");
                            break;
                    }

                }
            });

            int length = sbTeacher.length();
            if(length!=0){
                sbTeacher.deleteCharAt(length - 1);
                courseLiveScheduleVo.setTeacherNames(sbTeacher.toString());//教师名
            }
            int lengthCom = sbCom.length();
            if(lengthCom!=0){
                sbCom.deleteCharAt(lengthCom - 1);
                courseLiveScheduleVo.setCompereName(sbCom.toString());//主持人
            }
            int lengthAss = sbAss.length();
            if(lengthAss!=0){
                sbAss.deleteCharAt(lengthAss - 1);
                courseLiveScheduleVo.setAssistantName(sbAss.toString());//助教
            }
            int lengthCtrl = sbCtrl.length();
            if(lengthCtrl!=0){
                sbCtrl.deleteCharAt(lengthCtrl - 1);
                courseLiveScheduleVo.setControllerName(sbCtrl.toString());//场控
            }
            int lengthSys = sbSys.length();
            if(lengthSys!=0){
                sbSys.deleteCharAt(lengthSys - 1);
                courseLiveScheduleVo.setSysName(sbSys.toString());//场控
            }
            int lengthZks = sbZks.length();
            if(lengthZks!=0){
                sbZks.deleteCharAt(lengthZks - 1);
                courseLiveScheduleVo.setZksName(sbZks.toString());//场控
            }

            courseLiveScheduleVo.setExamType(courseLive.getCourse().getExamType().getText());

            courseLiveScheduleVo.setCourseLiveName(courseLive.getName());

            courseLiveScheduleVo.setSourceId(courseLive.getSourceId());

            courseLiveScheduleVos.add(courseLiveScheduleVo);

            courseLiveDic.put(courseLive.getId(), courseLiveScheduleVo);
        });

        // 处理滚动排课
        Iterator<CourseLiveScheduleVo> courseLiveScheduleVoIterator = courseLiveScheduleVos.iterator();
        while (courseLiveScheduleVoIterator.hasNext()) {
            CourseLiveScheduleVo temp = courseLiveScheduleVoIterator.next();
            // 如果是滚动排课，则只在源课程中追加课程名称
            if (temp.getSourceId() != null) {
                CourseLiveScheduleVo sourceCourseLive = courseLiveDic.get(temp.getSourceId());
                if (sourceCourseLive != null) {
                    sourceCourseLive.setCourseName(sourceCourseLive.getCourseName() + "，" + temp.getCourseName());

                    courseLiveScheduleVoIterator.remove();
                }
            }
        }

        return courseLiveScheduleVos;
    }

    @Override
    public List<CourseLive> findByCourseIdAndDateInAndTimeBeginAndTimeEnd(Long courseId, List<Date> dates, Integer timeBegin,
                                                                          Integer timeEnd) {
        return courseLiveRepository.findByCourseIdAndDateInAndTimeBeginAndTimeEnd(courseId, dates, timeBegin, timeEnd);
    }

    @Override
    public Long getCourseSubjectId(Long courseLiveId) {
        return courseRepository.getCourseSubjectId(courseLiveId);
    }

    @Override
    public List<CourseLive> findByDateAndTeacherId(Date dateBegin, Date dateEnd, Long teacherId ) {
        List<Sort.Order> list = Lists.newArrayList();
        list.add(new Sort.Order(Sort.Direction.ASC, "dateInt"));//日期
        list.add(new Sort.Order(Sort.Direction.ASC, "timeBegin"));//开始时间
        list.add(new Sort.Order(Sort.Direction.ASC, "timeEnd"));//结束时间
        return courseLiveRepository.findAll((root, criteriaQuery, criteriaBuilder) -> {
            criteriaQuery.distinct(true);

            List<Predicate> predicates = new ArrayList<>();

            Join<CourseLive, Course> course = root.join("course");
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(course.get("status"), CourseStatus.ZJAP));//大于助教安排状态
            predicates.add(criteriaBuilder.notEqual(course.get("courseCategory"), CourseCategory.VIDEO));//不为录播课
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("date"), dateBegin));//开始日期
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("date"), dateEnd));//完成日期
            predicates.add(criteriaBuilder.isNull(root.get("sourceId")));//不为滚动排课
            if (teacherId != null) {
                Join<CourseLive, CourseLiveTeacher> courseLiveTeachers = root.join("courseLiveTeachers", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(courseLiveTeachers.get("teacherId"), teacherId));
                predicates.add(criteriaBuilder.equal(courseLiveTeachers.get("confirm"), CourseConfirmStatus.QR));//课时统计为确认状态
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        }, new Sort(list));
    }

    @Override
    public int updateCourseLiveCategoryByStatus(Long liveId, CourseLiveCategory courseLiveCategory) {
        return courseLiveRepository.updateCourseLiveCategoryByStatus(liveId, courseLiveCategory);
    }

    @Override
    public List<TaskLiveDGTVo> getLiveByDGT() {
        List<TaskLiveDGTVo> taskVos = Lists.newArrayList();
        List<CourseLiveTeacher> taskDGT = courseLiveTeacherRepository.getTaskDGT();
        taskDGT.forEach(task -> {
            TaskLiveDGTVo vo = new TaskLiveDGTVo();
            vo.setRole(task.getTeacherType());// 角色根据type字段区分
            if (task.getTeacher() != null) {
                vo.setTeacherName(task.getTeacher().getName());
            }
            vo.setLiveId(task.getCourseLive().getId());
            vo.setLiveName(task.getCourseLive().getName());
            vo.setCourseId(task.getCourseLive().getCourseId());
            vo.setCourseName(task.getCourseLive().getCourse().getName());
            Integer begin = task.getCourseLive().getTimeBegin();
            Integer end = task.getCourseLive().getTimeEnd();
            if (null != begin && null != end) {
                String timeBegin = TimeRangeUtil.intToDateString(begin);
                String timeEnd = TimeRangeUtil.intToDateString(end);
                vo.setTimeRange(task.getCourseLive().getDate().toString() + " " + timeBegin + "-" + timeEnd);
            }
            vo.setConfirmKey(task.getConfirm().getValue());
            vo.setConfirmStatus(task.getConfirm().getText());
            vo.setLiveTeacherId(task.getId());
            taskVos.add(vo);
        });
        return taskVos;
    }

    @Override
    @Transactional
    public void submitCourseLiveTeacherDGT(Long liveId, Long liveITeacherd,
                                           Long teacherId, TeacherCourseLevel level) {
        Teacher oldTeacher ;
        String oldPhone ;
        Teacher newTeacher ;
        String newPhone ;
        CourseLive live = courseLiveRepository.findOne(liveId);
        CourseLiveTeacher data ;
        if (liveITeacherd == null) {
            throw new BadRequestException("直播教师数据id不能为空");
        }
        data = courseLiveTeacherRepository.findOne(liveITeacherd);//查找数据
        data.setConfirm(CourseConfirmStatus.DQR);
        if (null != level) {
            data.setTeacherCourseLevel(level);//更改级别
        }
        oldTeacher = data.getTeacher();
        oldPhone = oldTeacher.getPhone();//取出原教师电话
        newTeacher = teacherService.findOne(teacherId);
        newPhone = newTeacher.getPhone();//新教师电话
        data.setTeacherId(teacherId);//更改教师
        courseLiveTeacherRepository.save(data);//回写数据

        if (!oldPhone.equals(newPhone)) {
            //发送短信通知直播取消
            StringBuilder sb = new StringBuilder();
            sb.append("您沟通的课程:");
            sb.append(live.getCourse().getName());
            sb.append(",于");
            sb.append(live.getDate().toString()).append(" ").append(TimeRangeUtil.intToDateString(live.getTimeBegin())).append("-").append(TimeRangeUtil.intToDateString(live.getTimeEnd()));
            sb.append(" 安排的");
            sb.append(live.getCourse().getCourseCategory().getText());
            sb.append(":");
            sb.append(live.getName());
            sb.append(",已重新进行安排");
            if(SmsUtil.sendSms(oldPhone, sb.toString())){
                log.info("发送取消排课短信 : {} -> {}", newPhone, sb.toString());
            }else{
                log.info("fail : {} -> {}", newPhone, sb.toString());
            }
        }

        Course course = live.getCourse();//课程
        Object[] info  = new Object[8];

        info[0] = newTeacher.getName();
        info[1] = course.getName();
        info[2] = 1;
        info[3] = course.getCourseCategory().getText();//课程类型
        info[4] = data.getTeacherType().getText();//教师类型
        info[6] = newTeacher.getId();
        info[7] = data.getTeacherType();

        ConfirmToken confirmToken = new ConfirmToken();
        confirmToken.setTeacherId((Long) info[6]);
        confirmToken.setSourseId(course.getId());
        confirmToken.setTeacherType((TeacherType) info[7]);
        confirmToken.setToken(UUID.randomUUID().toString().replaceAll("-", ""));

        confirmTokenRepository.save(confirmToken);

        info[5] = apiHost + "/confirm/confirm.html?token=" + confirmToken.getToken();

        String content = String.format(SMSTemplate.COURSE_CONFIRM, info);

        if(SmsUtil.sendSms(newPhone, content)){
            log.info("发送教师确认短信 : {} -> {}", newPhone, content);
        }else{
            log.info("fail : {} -> {}", newPhone, content);
        }

    }

    @Override
	public List<CourseLive> findByCourseIdAndTeacherId(Long courseId, Long teacherId) {
		return courseLiveRepository.findAll((root, criteriaQuery, criteriaBuilder) -> {
            criteriaQuery.distinct(true);

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("courseId"), courseId));

            Join<CourseLive, CourseLiveTeacher> courseLiveTeachers = root.join("courseLiveTeachers");
            predicates.add(criteriaBuilder.equal(courseLiveTeachers.get("teacherId"), teacherId));
//				predicates.add(courseLiveTeachers.get("confirm")
//						.in(Arrays.asList(CourseConfirmStatus.DQR, CourseConfirmStatus.DGT)));

            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        });
	}

    @Override
    public List<CourseLive> getVideoRomInfo(Long videoRoomId, Date dateBegin, Date dateEnd) {
        List<Sort.Order> list = Lists.newArrayList();
        list.add(new Sort.Order(Sort.Direction.ASC, "dateInt"));//日期
        list.add(new Sort.Order(Sort.Direction.ASC, "timeBegin"));//开始时间
        list.add(new Sort.Order(Sort.Direction.ASC, "timeEnd"));//结束时间
        return courseLiveRepository.findAll((root, criteriaQuery, criteriaBuilder) -> {
            criteriaQuery.distinct(true);

            List<Predicate> predicates = new ArrayList<>();

            Join<CourseLive, Course> course = root.join("course");
            predicates.add(criteriaBuilder.equal(course.get("courseCategory"), CourseCategory.VIDEO));//录播课
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(course.get("status"), CourseStatus.WC));//完成状态
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("date"), dateBegin));//开始日期
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("date"), dateEnd));//完成日期
            predicates.add(criteriaBuilder.isNull(root.get("sourceId")));//不为滚动排课
            predicates.add(criteriaBuilder.equal(root.get("videoRoomId"),videoRoomId));//指定房间
            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        }, new Sort(list));
    }

    @Override
    public Boolean timeCheck(String beginString,String endString,Long roomId ,Date date,Long id){
        Integer timeBegin = Integer.parseInt(beginString.replace(":", ""));//开始时间
        Integer timeEnd = Integer.parseInt(endString.replace(":", ""));//结束时间
        //先校验时间是否冲突
        List<CourseLive> videoRomInfos = courseLiveRepository.findAll((root, criteriaQuery, criteriaBuilder) -> {
            criteriaQuery.distinct(true);

            List<Predicate> predicates = new ArrayList<>();

            Join<CourseLive, Course> course = root.join("course");
            predicates.add(criteriaBuilder.equal(course.get("courseCategory"), CourseCategory.VIDEO));//录播课
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(course.get("status"), CourseStatus.WC));//完成状态
            predicates.add(criteriaBuilder.equal(root.get("dateInt"), Integer.parseInt(DateformatUtil.format1(date ) )));//日期
            predicates.add(criteriaBuilder.isNull(root.get("sourceId")));//不为滚动排课
            predicates.add(criteriaBuilder.equal(root.get("videoRoomId"),roomId));//指定房间
            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        });
        for(CourseLive info:videoRomInfos){
            Integer begin = info.getTimeBegin();
            Integer end = info.getTimeEnd();
            if ((timeEnd > begin && timeEnd < end) || (timeBegin > begin && timeBegin < end)
                    || (timeBegin < begin && timeEnd > end)||(timeBegin.equals(begin)&&timeEnd.equals(end))) {
                if(!info.getId().equals(id)){
                    // 时间有冲突
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    @Transactional
    public CourseLive createCourseVideo(CreateCourseVideoDto dto)  {
        Integer timeBegin = Integer.parseInt(dto.getTimeBegin().replace(":", ""));//开始时间
        Integer timeEnd = Integer.parseInt(dto.getTimeEnd().replace(":", ""));//结束时间

        CourseLive courseLive=new CourseLive();
        courseLive.setCourseId(dto.getCourseId());
        courseLive.setVideoRoomId(dto.getRoomId());
        courseLive.setName(dto.getVideoName());
        courseLive.setDate(dto.getDate());
        courseLive.setDateInt(Integer.parseInt(DateformatUtil.format1(dto.getDate())));
        courseLive.setTimeBegin( timeBegin);
        courseLive.setTimeEnd( timeEnd);
        courseLive = courseLiveRepository.save(courseLive);
        Long id = courseLive.getId();

        List<CourseLiveTeacher> list=Lists.newArrayList();
        CourseLiveTeacher clt=new CourseLiveTeacher();//教师数据
        clt.setTeacherId(dto.getTeacherId());//id
        clt.setSubjectId(dto.getSubjectId());//科目
        clt.setTeacherType(TeacherType.JS);//讲师
        clt.setConfirm(CourseConfirmStatus.DQR);//待确认
        clt.setCourseLiveId(id);
        list.add(clt);
        dto.getPhotographerIds().forEach(photographerId->{
            CourseLiveTeacher photographer=new CourseLiveTeacher();//摄影师数据
            photographer.setTeacherId(photographerId);//id
            photographer.setTeacherType(TeacherType.SYS);///摄影师
            photographer.setConfirm(CourseConfirmStatus.DQR);//待确认
            photographer.setCourseLiveId(id);
            list.add(photographer);
        });
        if(null!=dto.getZkTeacherId()){
            CourseLiveTeacher zkTeacher=new CourseLiveTeacher();//质控数据
            zkTeacher.setTeacherId(dto.getZkTeacherId());//id
            zkTeacher.setTeacherType(TeacherType.ZKS);//质控师
            zkTeacher.setConfirm(CourseConfirmStatus.DQR);//待确认
            zkTeacher.setCourseLiveId(id);
            list.add(zkTeacher);
        }
        courseLive.setCourseLiveTeachers(list);
        return courseLive;
    }

    @Override
    public CourseLive updateCourseVideo(UpdateCourseVideoDto dto) {
        CourseLive sourceVideo = courseLiveRepository.findOne(dto.getId());//原数据
        sourceVideo.setCourseId(dto.getCourseId());
        sourceVideo.setVideoRoomId(dto.getRoomId());
        sourceVideo.setName(dto.getVideoName());
        sourceVideo.setDate(dto.getDate());
        sourceVideo.setDateInt(Integer.parseInt(DateformatUtil.format1(dto.getDate())));
        Integer timeBegin = Integer.parseInt(dto.getTimeBegin().replace(":", ""));//开始时间
        Integer timeEnd = Integer.parseInt(dto.getTimeEnd().replace(":", ""));//结束时间
        sourceVideo.setTimeBegin(timeBegin);
        sourceVideo.setTimeEnd(timeEnd);
        sourceVideo=courseLiveRepository.save(sourceVideo);//写回
        Long teacherId = dto.getTeacherId();//讲师
        Long zkTeacherId = dto.getZkTeacherId();//质控
        List<Long> photographerIds = dto.getPhotographerIds();//原摄影师ids
        List<String> newPhotographerIdStrings = photographerIds.stream().map(String::valueOf).collect(Collectors.toList());
        List<String> oldPhotographerIdStrings = Lists.newArrayList();
        List<CourseLiveTeacher> courseLiveTeachers = sourceVideo.getCourseLiveTeachers();
        Iterator<CourseLiveTeacher> iterator = courseLiveTeachers.iterator();
        while (iterator.hasNext()){
            CourseLiveTeacher courseLiveTeacher = iterator.next();
            TeacherType teacherType = courseLiveTeacher.getTeacherType();
            if(TeacherType.JS.equals(teacherType)){   //教师类型
                if(!teacherId.equals(courseLiveTeacher.getTeacherId())){  //原教师与现教师不符
                    courseLiveTeacher.setSubjectId(dto.getSubjectId());
                    courseLiveTeacher.setTeacherId(teacherId);
                    courseLiveTeacher.setConfirm(CourseConfirmStatus.DQR);
                    courseLiveTeacherRepository.save(courseLiveTeacher);
                }
            }else if(TeacherType.ZKS.equals(teacherType)){
                if(!courseLiveTeacher.getTeacherId().equals(zkTeacherId)) {  //原教师不等于现教师
                    if(null!=zkTeacherId) {
                        courseLiveTeacher.setTeacherId(zkTeacherId);
                        courseLiveTeacher.setConfirm(CourseConfirmStatus.DQR);
                        courseLiveTeacherRepository.save(courseLiveTeacher);
                        zkTeacherId=null;
                    }else{  //删除情况
                        iterator.remove();
                        courseLiveTeacherRepository.delete(courseLiveTeacher);
                    }
                }else{
                    zkTeacherId=null;
                }
            }else if(TeacherType.SYS.equals(teacherType)){ //摄影师情况
                String oldTeacherId = String.valueOf(courseLiveTeacher.getTeacherId());
                oldPhotographerIdStrings.add(oldTeacherId);  //原有摄影师集合
                if(!newPhotographerIdStrings.contains(oldTeacherId)) {//旧摄影师在新摄影师里没有删除
                    iterator.remove();
                    courseLiveTeacherRepository.delete(courseLiveTeacher);
                }
            }
        }
        if(null!=zkTeacherId){
            CourseLiveTeacher clt=new CourseLiveTeacher();
            clt.setTeacherId(zkTeacherId);//id
            clt.setTeacherType(TeacherType.ZKS);//类型
            clt.setConfirm(CourseConfirmStatus.DQR);//未确认
            clt.setCourseLiveId(dto.getId());
            courseLiveTeacherRepository.save(clt);
        }
        //新摄影师在旧摄影师里有不更改 没有添加 旧摄影师在新摄影师里没有删除
        for (String newTeacherId : newPhotographerIdStrings) {
            if(!oldPhotographerIdStrings.contains(newTeacherId)){//新摄影师在旧摄影师里没有添加
                CourseLiveTeacher clt=new CourseLiveTeacher();
                clt.setTeacherId(Long.valueOf(newTeacherId));//id
                clt.setTeacherType(TeacherType.SYS);//类型
                clt.setConfirm(CourseConfirmStatus.DQR);//未确认
                clt.setCourseLiveId(dto.getId());
                courseLiveTeacherRepository.save(clt);
                courseLiveTeachers.add(clt);
            }
        }
        return sourceVideo;
    }

    @Override
    public void sendCourseVideoConfirmSms(CourseLive courseVideo) {
        // 需要发送的数据<手机号，信息内容>
        Map<String, Object[]> datas = Maps.newHashMap();
        Course course = courseRepository.findOne( courseVideo.getCourseId());
        courseVideo.getCourseLiveTeachers().forEach(courseLiveTeacher -> {  //遍历数据
            if (null!=courseLiveTeacher.getTeacherType() &&CourseConfirmStatus.DQR.equals(courseLiveTeacher.getConfirm())) {
                Teacher teacher = teacherService.findOne(courseLiveTeacher.getTeacherId());
                String phone = teacher.getPhone();
                // 短信内容
                Object[] infos = datas.get(phone);
                if (infos == null) {
                    infos = new Object[8];
                    infos[0] = teacher.getName();//教师名
                    infos[1] = course.getName();//课程名
                    infos[2] = 1;//个数
                    infos[3] = course.getCourseCategory().getText();//课程类型
                    infos[4] = courseLiveTeacher.getTeacherType().getText();//教师类型
                    infos[6] = teacher.getId();
                    infos[7] = teacher.getTeacherType();
                    datas.put(phone, infos);
                } else {
                    infos[2] = (int) infos[2] + 1;
                }
                if(null!=courseLiveTeacher.getLastTeacherId()&&!courseLiveTeacher.getLastTeacherId().equals(courseLiveTeacher.getTeacherId())){  //有上一个教师情况 说明更换了教师  给原教师发送信息
                    Teacher lastTeacher = teacherService.findOne(courseLiveTeacher.getLastTeacherId());
                    //发送短信通知直播取消
                    StringBuffer sb = new StringBuffer();
                    sb.append("您好,");
                    sb.append(lastTeacher.getName());
                    sb.append("老师,\"");
                    sb.append(course.getName());
                    sb.append("\"课程中给您安排的\"");
                    sb.append(courseVideo.getName());
                    sb.append("\"的课程安排已取消");
                    SmsUtil.sendSms(lastTeacher.getPhone(), sb.toString());
                    log.info("发送取消排课短信 : {} -> {}", lastTeacher.getPhone(), sb.toString());
                }
            }

        });
        // 发送信息
        datas.entrySet().forEach(data -> {
            ConfirmToken confirmToken = new ConfirmToken();
            confirmToken.setTeacherId((Long) data.getValue()[6]);
            confirmToken.setSourseId(course.getId());
            confirmToken.setTeacherType((TeacherType) data.getValue()[7]);
            confirmToken.setToken(UUID.randomUUID().toString().replaceAll("-", ""));

            confirmTokenRepository.save(confirmToken);

            data.getValue()[5] = apiHost + "/confirm/confirm.html?token=" + confirmToken.getToken();

            String content = String.format(SMSTemplate.COURSE_CONFIRM, data.getValue());

            SmsUtil.sendSms(data.getKey(), content);

            log.info("发送教师确认短信 : {} -> {}", data.getKey(), content);
        });
    }

    @Override
    public void courseVideoCancel(Long id, String reason) {
        CourseLive courseVideo = courseLiveRepository.findOne(id);
        if(null==courseVideo){
            throw new BadRequestException("id错误");
        }
        Integer dateInt = courseVideo.getDateInt();
        Integer timeBegin = courseVideo.getTimeBegin();
        Integer timeEnd = courseVideo.getTimeEnd();
        String time=TimeformatUtil.format(timeBegin) + "-" + TimeformatUtil.format(timeEnd);
        courseVideo.getCourseLiveTeachers().forEach(data->{
            Teacher teacher = data.getTeacher();
            StringBuffer sb = new StringBuffer();
            sb.append(teacher.getName());
            sb.append("老师,");
            sb.append(dateInt+" ");
            sb.append(time);
            sb.append("安排的\"");
            sb.append(courseVideo.getName());
            sb.append("\"录课计划");
            if(StringUtils.isNotBlank(reason)){
                sb.append("因\"").append(reason).append("\"");
            }
            sb.append("已取消");
            SmsUtil.sendSms(teacher.getPhone(), sb.toString());
            log.info("发送取消排课短信 : {} -> {}", teacher.getPhone(), sb.toString());
        });
        courseLiveRepository.delete(id);
    }

    @Override
    @Transactional
    public void importExcel(List<List<List<String>>> list, Long courseId) {
        Course course = courseRepository.findOne(courseId);
        int ordinal = course.getStatus().ordinal();
        if(1<ordinal){
            throw new BadRequestException("该课程已完成教师安排,禁止导入");
        }
        ExamType examType = course.getExamType();
        Iterator<CourseLive> iterator = course.getCourseLives().iterator();
        while (iterator.hasNext()){  //清空全部子集
            CourseLive next = iterator.next();
            courseLiveRepository.delete(next);
            iterator.remove();
        }
        for (List<List<String>> sheet : list) {
            for (int i = 1; i < sheet.size(); i++) { //跳过表头
                List<String> row = sheet.get(i);
                CourseLive live=new CourseLive();
                live.setCourseId(courseId);
                String dateString = row.get(0);
                Date date;
                Integer timeBegin;
                Integer timeEnd;
                if(StringUtils.isBlank(dateString)){
                    throw new BadRequestException(i+1+"行授课日期不能为空");
                }else{
                    try {
                        date = HSSFDateUtil.getJavaDate(Double.parseDouble(dateString));
//                        date = DateformatUtil.parse0(dateString);
                        live.setDate(date);
                        live.setDateInt(Integer.parseInt(DateformatUtil.format1(date)));
                    } catch (Exception e) {
                        throw new BadRequestException(i+1+"行授课日期格式错误");
                    }
                }
                String timeString = row.get(1);
                if(StringUtils.isBlank(timeString)){
                    throw new BadRequestException(i+1+"行授课时间不能为空");
                }else{
                    try {
                        String[] times = timeString.split("-");
                        timeBegin = Integer.parseInt(times[0].replace(":", ""));
                        timeEnd = Integer.parseInt(times[1].replace(":", ""));
                        live.setTimeBegin(timeBegin);
                        live.setTimeEnd(timeEnd);
                    } catch (NumberFormatException e) {
                        throw new BadRequestException(i+1+"行授课时间格式错误");
                    }
                }
                String nameString = row.get(2);
                if(StringUtils.isBlank(nameString)){
                    throw new BadRequestException(i+1+"行授课内容不能为空");
                }
                live.setName(nameString);
                String categoryString = row.get(3);
                CourseLiveCategory courseLiveCategory = CourseLiveCategory.findByName(categoryString);

                if (courseLiveCategory == null) {
                    throw new BadRequestException(i + 1 + "行授课类型为空");
                }

                live.setCourseLiveCategory(courseLiveCategory);

                live=courseLiveRepository.save(live);//储存直播数据

                CourseLiveTeacher clt=new CourseLiveTeacher();
                clt.setCourseLiveId(live.getId());
                clt.setConfirm(CourseConfirmStatus.DQR);
                clt.setTeacherType(TeacherType.JS);
                String subjectString = row.get(4);
                if(StringUtils.isNotBlank(subjectString)){
                    List<Subject> byName = subjectRepository.findByExamTypeAndName(examType,subjectString);
                    if(null!=byName&&!byName.isEmpty()){
                       clt.setSubjectId(byName.get(0).getId());
                    }
                }
                String levelString = row.get(5);
                if(StringUtils.isNotBlank(levelString)){
                    TeacherCourseLevel byName = TeacherCourseLevel.findByName(levelString);
                    clt.setTeacherCourseLevel(byName);
                }
                courseLiveTeacherRepository.save(clt);
            }
        }
    }

    @Override
    @Transactional
    public void importCourse(List<List<List<String>>> list, Long courseId) {
        Course course = courseRepository.findOne(courseId);
        course.setStatus(CourseStatus.JSAP);
        courseRepository.save(course);

        int ordinal = course.getStatus().ordinal();
        if(1<ordinal){
            throw new BadRequestException("该课程已完成教师安排,禁止导入");
        }
        ExamType examType = course.getExamType();
        Iterator<CourseLive> iterator = course.getCourseLives().iterator();
        while (iterator.hasNext()){  //清空全部子集
            CourseLive next = iterator.next();
            courseLiveRepository.delete(next);
            iterator.remove();
        }
        for (List<List<String>> sheet : list) {
            for (int i = 1; i < sheet.size(); i++) { //跳过表头
                List<String> row = sheet.get(i);
                CourseLive live=new CourseLive();
                live.setCourseId(courseId);
                String dateString = row.get(0);
                Date date;
                Integer timeBegin;
                Integer timeEnd;
                if(StringUtils.isBlank(dateString)){
                    throw new BadRequestException(i+1+"行授课日期不能为空");
                }else{
                    try {
                        date = HSSFDateUtil.getJavaDate(Double.parseDouble(dateString));
//                        date = DateformatUtil.parse0(dateString);
                        live.setDate(date);
                        live.setDateInt(Integer.parseInt(DateformatUtil.format1(date)));
                    } catch (Exception e) {
                        throw new BadRequestException(i+1+"行授课日期格式错误");
                    }
                }
                String timeString = row.get(1);
                if(StringUtils.isBlank(timeString)){
                    throw new BadRequestException(i+1+"行授课时间不能为空");
                }else{
                    try {
                        String[] times = timeString.split("-");
                        timeBegin = Integer.parseInt(times[0].replace(":", ""));
                        timeEnd = Integer.parseInt(times[1].replace(":", ""));
                        live.setTimeBegin(timeBegin);
                        live.setTimeEnd(timeEnd);
                    } catch (NumberFormatException e) {
                        throw new BadRequestException(i+1+"行授课时间格式错误");
                    }
                }
                String nameString = row.get(2);
                if(StringUtils.isBlank(nameString)){
                    throw new BadRequestException(i+1+"行授课内容不能为空");
                }
                live.setName(nameString);

                String categoryString = row.get(3);
                CourseLiveCategory courseLiveCategory = CourseLiveCategory.findByName(categoryString);

                if (courseLiveCategory == null) {
                    throw new BadRequestException(i + 1 + "行授课类型为空");
                }

                live.setCourseLiveCategory(courseLiveCategory);

                live=courseLiveRepository.save(live);//储存直播数据

                /// 讲师
                CourseLiveTeacher clt=new CourseLiveTeacher();
                clt.setCourseLiveId(live.getId());
                clt.setConfirm(CourseConfirmStatus.DQR);
                clt.setTeacherType(TeacherType.JS);
                String subjectString = row.get(4);
                if(StringUtils.isNotBlank(subjectString)){
                    List<Subject> byName = subjectRepository.findByExamTypeAndName(examType,subjectString);
                    if(null!=byName&&!byName.isEmpty()){
                        clt.setSubjectId(byName.get(0).getId());
                    }
                }

                String teacherName = row.get('F' - 'A');
                if(StringUtils.isNotBlank(teacherName)){
                    Teacher teacher = null;
                    if (clt.getSubjectId() != null) {
                        teacher = teacherService.findByNameAndSubjectId(teacherName, clt.getSubjectId());
                    } else {
                        teacher = teacherService.findByName(teacherName);
                    }
                    if (teacher != null) {
                        List<TeacherScoreBean> availableTeachers = teacherService.getAvailableTeachers(live.getDate(), live.getTimeBegin(), live.getTimeEnd(),
                                course.getExamType(), course.getSubjectId(), clt.getTeacherCourseLevel(), courseId);
                        for (TeacherScoreBean availableTeacher : availableTeachers) {
                            if (availableTeacher.getId().equals(teacher.getId())) {
                                clt.setTeacherId(teacher.getId());
                                break;
                            }
                        }
                    }
                }

                if (clt.getTeacherId() != null && clt.getSubjectId() != null) {
                    TeacherSubject teacherSubject = teacherSubjectRepository.findByTeacherIdAndSubjectId(clt.getTeacherId(), clt.getSubjectId());
                    clt.setTeacherCourseLevel(teacherSubject.getTeacherCourseLevel());
                }
                courseLiveTeacherRepository.save(clt);
            }
        }
    }

    @Override
    @Transactional
    public void updateDateTimeBatch(UpdateDateTimeBatchDto dto) {
        List<Long> dtoIdList = dto.getCourseLiveIds();
        int len=dtoIdList.size();//选中元素数
        Integer index = dto.getIndex();//指定下标索引值
        Course one = courseRepository.findOne(dto.getCourseId());
        List<CourseLive> courseLives = one.getCourseLives();
        int liveSize = courseLives.size();//总长度
        int max=liveSize-len;//最大可选下标
        if(index>=max){//超过最大值设为最大值
            index=max;
        }
        courseLives.sort((o1,o2)->{  //开始日期排序
            int result=o1.getDateInt()-o2.getDateInt();//日期排序
            result=result==0?o1.getTimeBegin()-o2.getTimeBegin():result;//开始时间排序
            result=result==0?o1.getTimeEnd()-o2.getTimeEnd():result;//结束时间排序
            result=result==0?o1.getId().intValue()-o2.getId().intValue():result;
            return result;
        });
        List<Map> mapList=Lists.newArrayList();
        for (CourseLive courseLive : courseLives) {  //将原有时间存储
            Map<String,Object> map=Maps.newHashMap();
            map.put("date",courseLive.getDate());
            map.put("timeBegin",courseLive.getTimeBegin());
            map.put("timeEnd",courseLive.getTimeEnd());
            map.put("dateInt",courseLive.getDateInt());
            mapList.add(map);
        }
        List<String> dtoIds=dtoIdList.stream().map(String::valueOf).collect(Collectors.toList());//转成string类型集合
        List<String> oldIds=courseLives.stream().map(bean->String.valueOf(bean.getId())).collect(Collectors.toList());//转成string类型集合
        //如果是勾选id 从id集合中删除
        oldIds.removeIf(dtoIds::contains);
        String[] newIds=new String[liveSize];
        for (int i = 0; i < len; i++) {  //将选中id插入到指定位置
            String newId=dtoIds.get(i);
            newIds[index+i]=newId;
        }
        for (String oldId : oldIds) {
            for (int i = 0; i < newIds.length; i++) {
                String newId=newIds[i];
                if(StringUtils.isBlank(newId)){
                    newIds[i]=oldId;
                    break;
                }
            }
        }
        for (int i = 0; i < newIds.length; i++) {
            String id=newIds[i];
            for (CourseLive courseLive : courseLives) {
                if(id.equals(String.valueOf(courseLive.getId()))){
                    if(null!=courseLive.getSourceId()){
                        break;
                    }
                    List<CourseLive> lives = courseLiveRepository.findByIdOrSourceId(courseLive.getId());
                    for (CourseLive live : lives) {
                        Map map = mapList.get(i);
                        Date date=(Date)map.get("date");
                        Integer timeBegin=(Integer)map.get("timeBegin");
                        Integer timeEnd=(Integer)map.get("timeEnd");
                        Integer dateInt=(Integer)map.get("dateInt");
                        live.setDate(date);
                        live.setDateInt(dateInt);
                        live.setTimeBegin(timeBegin);
                        live.setTimeEnd(timeEnd);
                        live = courseLiveRepository.save(courseLive);
                        for (CourseLiveTeacher courseLiveTeacher : live.getCourseLiveTeachers()) {
                            courseLiveTeacher.setTeacherId(null);//清空教师
                            courseLiveTeacher.setConfirm(CourseConfirmStatus.DQR);//状态待确认
                            courseLiveTeacherRepository.save(courseLiveTeacher);
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<Long> findBySourceIdIn(List<Long> courseLiveIds) {
        return courseLiveRepository.findBySourceIdIn(courseLiveIds);
    }

    @Override
    public Boolean updateDateTime(UpdateDateTimeDto dto) {
        List<CourseLive> courseLives = courseLiveRepository.findByIdOrSourceId(dto.getCourseLiveId());
        for (CourseLive courseLive : courseLives) {
            Date date = dto.getDate();
            Integer timeBegin = dto.getTimeBegin();
            Integer timeEnd = dto.getTimeEnd();
            courseLive.setDate(date);
            courseLive.setDateInt(Integer.parseInt(DateformatUtil.format1(date)));
            courseLive.setTimeBegin(timeBegin);
            courseLive.setTimeEnd(timeEnd);
            courseLive = courseLiveRepository.save(courseLive);
            for (CourseLiveTeacher courseLiveTeacher : courseLive.getCourseLiveTeachers()) {
                courseLiveTeacher.setTeacherId(null);//清空教师
                courseLiveTeacher.setConfirm(CourseConfirmStatus.DQR);//状态待确认
                courseLiveTeacherRepository.save(courseLiveTeacher);
            }
        }
        return true;
    }

}
