package com.huatu.tiku.schedule.biz.service.imple;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;

import com.google.common.collect.Sets;
import com.huatu.tiku.schedule.biz.enums.*;
import com.huatu.tiku.schedule.biz.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.schedule.base.exception.BadRequestException;
import com.huatu.tiku.schedule.base.service.impl.BaseServiceImpl;
import com.huatu.tiku.schedule.biz.constant.SMSTemplate;
import com.huatu.tiku.schedule.biz.domain.ConfirmToken;
import com.huatu.tiku.schedule.biz.domain.Course;
import com.huatu.tiku.schedule.biz.domain.CourseLive;
import com.huatu.tiku.schedule.biz.domain.CourseLiveTeacher;
import com.huatu.tiku.schedule.biz.domain.Teacher;
import com.huatu.tiku.schedule.biz.domain.delete.DeleteCourseDetail;
import com.huatu.tiku.schedule.biz.service.CourseService;
import com.huatu.tiku.schedule.biz.util.SmsUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CourseServiceImpl extends BaseServiceImpl<Course, Long> implements CourseService {

    private final CourseRepository courseRepository;

    private final DeleteCourseDetailRepository deleteCourseDetailRepository;

    private final CourseLiveRepository courseLiveRepository;

    private final ConfirmTokenRepository confirmTokenRepository;

    private final CourseLiveTeacherRepository courseLiveTeacherRepository;


    @Value("${api.host}")
    private String apiHost;

    @Autowired
    public CourseServiceImpl(CourseRepository courseRepository, DeleteCourseDetailRepository deleteCourseDetailRepository,
                             CourseLiveRepository courseLiveRepository,
                             ConfirmTokenRepository confirmTokenRepository, CourseLiveTeacherRepository courseLiveTeacherRepository) {
        this.courseRepository = courseRepository;
        this.deleteCourseDetailRepository = deleteCourseDetailRepository;
        this.courseLiveRepository = courseLiveRepository;
        this.confirmTokenRepository = confirmTokenRepository;
        this.courseLiveTeacherRepository = courseLiveTeacherRepository;
    }

    @Override
    public List<Course> getCourseListByStatus(CourseStatus status) {
        Specification<Course> querySpecific = (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("status"),status));
            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
        return courseRepository.findAll(querySpecific,new Sort(Sort.Direction.DESC, "id"));
    }

    @Override
    public Page<Course> getCourseListZJ(ExamType examType, String name, Long id, Long subjectId, Date dateBegin,
                                      Date dateEnd, String teacherName, CourseStatus status, Pageable page) {
        Specification<Course> querySpecific = (root, criteriaQuery, criteriaBuilder) -> {
            criteriaQuery.distinct(true);

            if (!Long.class.equals(criteriaQuery.getResultType())) {
                root.fetch("subject", JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();

            if (examType != null) {
                predicates.add(criteriaBuilder.equal(root.get("examType"), examType));
            }
//				else{
//                    predicates.add(criteriaBuilder.notEqual(root.get("examType"), ExamType.MS));
//				}
            if (subjectId != null) {
                predicates.add(criteriaBuilder.equal(root.get("subject"), subjectId));
            }
            if (!Strings.isNullOrEmpty(name)) {
                predicates.add(criteriaBuilder.like(root.get("name"), "%" + name + "%"));
            }
            if (id != null) {
                predicates.add(criteriaBuilder.equal(root.get("id"), id));
            }
            Join<Course, CourseLive> courseLives = null;
            if (dateBegin != null && dateEnd != null) {
                courseLives = root.join("courseLives");

                predicates.add(criteriaBuilder.between(courseLives.get("date"), dateBegin, dateEnd));
            }
            if (!Strings.isNullOrEmpty(teacherName)) {
                if (courseLives == null) {
                    courseLives = root.join("courseLives");
                }

                Join<CourseLive, CourseLiveTeacher> courseLiveTeachers = courseLives.join("courseLiveTeachers");

                Join<CourseLiveTeacher, Teacher> teachers = courseLiveTeachers.join("teacher");

                predicates.add(criteriaBuilder.like(teachers.get("name"), "%" + teacherName + "%"));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }else{
                predicates.add(root.get("status").in(Lists.newArrayList(CourseStatus.ZJAP,CourseStatus.ZJQR,CourseStatus.WC)));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable = new PageRequest(page.getPageNumber(), page.getPageSize(), sort);
        return courseRepository.findAll(querySpecific, pageable);
    }

    @Override
    public Page<Course> getCourseList(CourseCategory category,List<ExamType> examTypes, String name, Long id, Long subjectId, Date dateBegin,
                                      Date dateEnd, String teacherName, CourseStatus status, Pageable page) {
        Specification<Course> querySpecific = (root, criteriaQuery, criteriaBuilder) -> {
            criteriaQuery.distinct(true);

            if (!Long.class.equals(criteriaQuery.getResultType())) {
                root.fetch("subject", JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();

            if (category != null ) {
                predicates.add(criteriaBuilder.equal(root.get("courseCategory"), category));
            }
            if (examTypes != null && !examTypes.isEmpty()) {
                predicates.add(root.get("examType").in(examTypes));
            }
            if (subjectId != null) {
                predicates.add(criteriaBuilder.equal(root.get("subject"), subjectId));
            }
            if (!Strings.isNullOrEmpty(name)) {
                predicates.add(criteriaBuilder.like(root.get("name"), "%" + name + "%"));
            }
            if (id != null) {
                predicates.add(criteriaBuilder.equal(root.get("id"), id));
            }
            Join<Course, CourseLive> courseLives = null;
            if (dateBegin != null && dateEnd != null) {
                courseLives = root.join("courseLives");

                predicates.add(criteriaBuilder.between(courseLives.get("date"), dateBegin, dateEnd));
            }
            if (!Strings.isNullOrEmpty(teacherName)) {
                if (courseLives == null) {
                    courseLives = root.join("courseLives");
                }

                Join<CourseLive, CourseLiveTeacher> courseLiveTeachers = courseLives.join("courseLiveTeachers");

                Join<CourseLiveTeacher, Teacher> teachers = courseLiveTeachers.join("teacher");

                predicates.add(criteriaBuilder.like(teachers.get("name"), "%" + teacherName + "%"));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable = new PageRequest(page.getPageNumber(), page.getPageSize(), sort);
        return courseRepository.findAll(querySpecific, pageable);
    }

    @Override
    public Page<Course> getCourseList(List<CourseCategory> categorys,List<ExamType> examTypes, String name, Long id, Long subjectId, Date dateBegin,
                                      Date dateEnd, String teacherName, CourseStatus status, Pageable page) {
        Specification<Course> querySpecific = (root, criteriaQuery, criteriaBuilder) -> {
            criteriaQuery.distinct(true);

            if (!Long.class.equals(criteriaQuery.getResultType())) {
                root.fetch("subject", JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();

            if (categorys != null&&!categorys.isEmpty() ) {
                predicates.add(root.get("courseCategory").in(categorys));
            }
            if (examTypes != null && !examTypes.isEmpty()) {
                predicates.add(root.get("examType").in(examTypes));
            }
            if (subjectId != null) {
                predicates.add(criteriaBuilder.equal(root.get("subject"), subjectId));
            }
            if (!Strings.isNullOrEmpty(name)) {
                predicates.add(criteriaBuilder.like(root.get("name"), "%" + name + "%"));
            }
            if (id != null) {
                predicates.add(criteriaBuilder.equal(root.get("id"), id));
            }
            Join<Course, CourseLive> courseLives = null;
            if (dateBegin != null && dateEnd != null) {
                courseLives = root.join("courseLives");

                predicates.add(criteriaBuilder.between(courseLives.get("date"), dateBegin, dateEnd));
            }
            if (!Strings.isNullOrEmpty(teacherName)) {
                if (courseLives == null) {
                    courseLives = root.join("courseLives");
                }

                Join<CourseLive, CourseLiveTeacher> courseLiveTeachers = courseLives.join("courseLiveTeachers");

                Join<CourseLiveTeacher, Teacher> teachers = courseLiveTeachers.join("teacher");

                predicates.add(criteriaBuilder.like(teachers.get("name"), "%" + teacherName + "%"));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable = new PageRequest(page.getPageNumber(), page.getPageSize(), sort);
        return courseRepository.findAll(querySpecific, pageable);
    }

    @Override
    @Transactional
    public Map<String, Object> submitCourseLive(Long id) {
        // 返回结果
        Map<String, Object> result = Maps.newHashMap();

        // 错误信息
        StringBuilder message = new StringBuilder();

        // 获取课程
        Course course = courseRepository.findOne(id);

        if (course == null) {
            message.append("课程不存在");
        } else {
            // 如果状态为直播安排
            if (CourseStatus.ZBAP.equals(course.getStatus())) {
                List<CourseLive> courseLives = course.getCourseLives();

                if (courseLives.isEmpty()) {
                    message.append("未添加直播");
                } else {
                    // 未添加教师的直播
                    List<CourseLive> emptyCourseLives = Lists.newArrayList();

                    for (CourseLive courseLive : courseLives) {
                        List<CourseLiveTeacher> courseLiveTeachers = courseLive.getCourseLiveTeachers();

                        if (courseLiveTeachers.isEmpty()) {
                            emptyCourseLives.add(courseLive);
                        }
                    }

                    if (message.length() == 0) {
                        if (emptyCourseLives.size() == courseLives.size()) {
                            message.append("未添加直播安排");
                        } else {
                            // 删除无效直播
                            courseLiveRepository.delete(emptyCourseLives);

                            // 更新课程状态
                            course.setStatus(CourseStatus.JSAP);

                            courseRepository.save(course);
                        }
                    }
                }
            } else {
                message.append("课程状态错误");
            }
        }

        if (message.length() == 0) {
            result.put("success", true);
            result.put("message", "提交成功");
        } else {
            result.put("success", false);
            result.put("message", message.toString());
        }

        return result;
    }

    @Override
    public List<Course> rollingCourse(Long id, List<Date> dates) {
        return courseRepository.findAll((root, criteriaQuery, criteriaBuilder) -> {
            criteriaQuery.distinct(true);

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.notEqual(root.get("id"), id));

            predicates.add(criteriaBuilder.equal(root.get("status"), CourseStatus.WC));//完成状态

//                predicates.add(criteriaBuilder.equal(root.get("courseCategory"), CourseCategory.LIVE));//直播课程

            Join<Course, CourseLive> courseLives = root.join("courseLives");

            if (dates != null && dates.size() > 0) {
                predicates.add(courseLives.get("date").in(dates));
            }
            predicates.add(criteriaBuilder.isNull(courseLives.get("sourceId")));//不为滚动排课

            predicates.add(criteriaBuilder.notEqual(root.get("courseCategory"), CourseCategory.VIDEO));

            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        });
    }

    @Override
    public Map<String, Object> submitCourseLiveTeacher(Course course) {
        // 返回结果
        Map<String, Object> result = Maps.newHashMap();

        // 错误信息
        StringBuilder message = new StringBuilder();

        if (course == null) {
            message.append("课程不存在");
        } else {
            // 检测课程状态
            if (course.getStatus().ordinal() >= CourseStatus.JSAP.ordinal()) {
                // 课程直播列表
                List<CourseLive> courseLives = course.getCourseLives();

                if (courseLives.isEmpty()) {
                    message.append("未添加直播");
                } else {
                    // 未添加教师的直播
                    List<CourseLive> emptyCourseLives = Lists.newArrayList();
                    boolean flag = false;
                    for (CourseLive courseLive : courseLives) {
                        if (message.length() == 0&&null==courseLive.getSourceId()) {
                            // 校验直播内容
                            if (Strings.isNullOrEmpty(courseLive.getName())) {
                                message.append("授课内容为必填项");
                                break;
                                // 校验直播间
                            }
							if(ExamType.MS.equals(course.getExamType())&&null==courseLive.getCourseLiveCategory()&& CourseCategory.XXK.equals(course.getCourseCategory())){//面试类型 授课类型没填时 课程类型为线下
//                            if (ExamType.MS.equals(course.getExamType()) && null == courseLive.getCourseLiveCategory()) {//面试类型 授课类型没填时
                                message.append("面试授课类型为必填项");
                                break;
                            } else {
                                List<CourseLiveTeacher> courseLiveTeachers = courseLive.getCourseLiveTeachers();

                                if (courseLiveTeachers.isEmpty()) {
                                    emptyCourseLives.add(courseLive);
                                } else {
                                    for (CourseLiveTeacher courseLiveTeacher : courseLiveTeachers) {
                                        if(TeacherType.JS.equals(courseLiveTeacher.getTeacherType())) {//教师数据 进行校验
                                            // 校验必填项
                                            if (courseLiveTeacher.getTeacherId() == null) {
                                                message.append("授课教师为必填项");
                                                break;
                                            } else if (courseLiveTeacher.getSubjectId() == null) {
                                                message.append("科目为必填项");
                                                break;
                                            }
                                        }
                                        //courseLiveTeacher.setConfirm(CourseConfirmStatus.DQR);
                                    }
                                }
                                if (null == courseLive.getSourceId()) {//如果不是滚动排课
                                    flag = true;
                                }
                            }
                        }
                    }

                    if (message.length() == 0) {
                        if (emptyCourseLives.size() == courseLives.size()) {
                            message.append("未添加课程安排");
                        } else {
                            // 删除无效直播
                            courseLiveRepository.delete(emptyCourseLives);
                            if (course.getStatus().equals(CourseStatus.JSAP)) {
                                // 更新课程状态
                                if (flag) {//有一个不是滚动排课的就是到教师确认
                                    course.setStatus(CourseStatus.JSQR);
                                } else {//全是滚动排课 课程状态到完成
                                    course.setStatus(CourseStatus.WC);
                                }
                                courseRepository.save(course);
                            }
                        }
                    }
                }
            } else {
                message.append("课程状态错误");
            }
        }

        if (message.length() == 0) {
            result.put("success", true);
            result.put("message", "提交成功");
        } else {
            result.put("success", false);
            result.put("message", message.toString());
        }

        return result;
    }

    @Override
    public Map<String, Object> submitCourseLiveAssitant(Long id) {
        // 返回结果
        Map<String, Object> result = Maps.newHashMap();
        // 错误信息
        StringBuilder message = new StringBuilder();
       // 获取课程
        Course course = courseRepository.findOne(id);
        if (course == null) {
            message.append("课程不存在");
        } else {
            // 检测课程状态
            if (CourseStatus.ZJAP.ordinal()<=course.getStatus().ordinal()) {  //助教安排状态
                // 课程直播列表
                List<CourseLive> courseLives = course.getCourseLives();
                if (courseLives.isEmpty()) {
                    message.append("未添加直播");
                } else {
                    // 未添加教师的直播
                    List<CourseLive> emptyCourseLives = Lists.newArrayList();
                    boolean flag = false;
                    for (CourseLive courseLive : courseLives) {
                        if (message.length() == 0&&null==courseLive.getSourceId()) {  //TODO 滚动排课跳过验证  后面完善与元课程进行同步
                            // 校验直播内容
                            if (Strings.isNullOrEmpty(courseLive.getName())) {
                                message.append("授课内容为必填项");
                                break;
                            }
                                List<CourseLiveTeacher> courseLiveTeachers = courseLive.getCourseLiveTeachers();
                                if (courseLiveTeachers.isEmpty()) {
                                    emptyCourseLives.add(courseLive);
                                } else {
                                    for (CourseLiveTeacher courseLiveTeacher : courseLiveTeachers) {
                                        if(!TeacherType.JS.equals(courseLiveTeacher.getTeacherType())) {//不为教师数据 进行校验
                                            // 校验必填项
                                            if (courseLiveTeacher.getTeacherId() == null) {
                                                message.append("教师为必填项");
                                                break;
                                            } else if (courseLiveTeacher.getSubjectId() == null&&TeacherType.ZJ.equals(courseLiveTeacher.getTeacherType())) {
                                                message.append("科目为必填项");//且为助教类型 校验科目
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (null == courseLive.getSourceId()) {//如果不是滚动排课
                                    flag = true;
                                }
                        }
                    }

                    if (message.length() == 0) {
                        if (emptyCourseLives.size() == courseLives.size()) {
                            message.append("未添加课程安排");
                        } else {
                            // 删除无效直播
                            courseLiveRepository.delete(emptyCourseLives);
                            if (course.getStatus().equals(CourseStatus.ZJAP)) {
                                // 更新课程状态
                                if (flag) {//有一个不是滚动排课的就到助教确认
                                    course.setStatus(CourseStatus.ZJQR);
                                } else {//全是滚动排课 课程状态到完成
                                    course.setStatus(CourseStatus.WC);
                                }
                                courseRepository.save(course);
                            }
                        }
                    }
                }
            } else {
                message.append("课程状态错误");
            }
        }

        if (message.length() == 0) {
            result.put("success", true);
            result.put("message", "提交成功");
        } else {
            result.put("success", false);
            result.put("message", message.toString());
        }

        return result;
    }

    @Override
    public List<Course> findAllByLives(List<Long> liveIds) {
        return courseRepository.findAllByLives(liveIds);
    }

    @Override
    public void updateStatus(Course course ) {
        List<CourseLive> courseLives = course.getCourseLives();
        for (CourseLive courseLive : courseLives) { //循环直播
            if(courseLive.getSourceId() != null){//滚动排课直播
                continue ; //跳过此次直播判断
            }
            List<CourseLiveTeacher> courseLiveTeachers = courseLive.getCourseLiveTeachers();
            for (CourseLiveTeacher courseLiveTeacher : courseLiveTeachers) {
                if (!CourseConfirmStatus.QR.equals(courseLiveTeacher.getConfirm())) {//如果有未确认 直接返回
                    return;
                }
            }

        }  //判断完成 所有直播数据全部确认 进行更改状态判断
        Boolean assistantFlag = course.getAssistantFlag();//需要助教
        Boolean compereFlag = course.getCompereFlag();
        Boolean controllerFlag = course.getControllerFlag();
        if (!assistantFlag && !compereFlag && !controllerFlag) {  //如果助教全没选 直接进入完成状态
            course.setStatus(CourseStatus.WC);//状态改为完成
        } else {  //需要任意助教
            CourseStatus status = course.getStatus();//课程现在的状态
            if(CourseStatus.JSQR.equals(status)) {  //如果是教师确认状态 是正常流程  到助教安排 初始化助教数据
                for (CourseLive courseLive : courseLives) {  //循环直播
                    if(null==courseLive.getSourceId()) { //非滚动排课进行初始化
                        if (assistantFlag) {
                            CourseLiveTeacher courseLiveTeacher = new CourseLiveTeacher();
                            courseLiveTeacher.setCourseLiveId(courseLive.getId());//直播id
                            courseLiveTeacher.setTeacherType(TeacherType.ZJ);//助教类型
                            courseLiveTeacher.setConfirm(CourseConfirmStatus.DQR);//待确认
                            courseLiveTeacher.setTeacherCourseLevel(TeacherCourseLevel.ZZZJ);//专职助教
                            courseLiveTeacher.setSubjectId(course.getSubjectId());//写入课程科目id
                            CourseLiveTeacher date = courseLive.getCourseLiveTeachers().get(0);
                            Long teacherSubjectId = date.getSubjectId();
                            if (teacherSubjectId != null && TeacherType.JS.equals(date.getTeacherType())) {
                                courseLiveTeacher.setSubjectId(teacherSubjectId);//第一条数据教师类型  且有科目 写入科目
                            }
                            courseLiveTeacherRepository.save(courseLiveTeacher);
                        }
                        if (compereFlag) {
                            CourseLiveTeacher courseLiveTeacher = new CourseLiveTeacher();
                            courseLiveTeacher.setCourseLiveId(courseLive.getId());//直播id
                            courseLiveTeacher.setTeacherType(TeacherType.ZCR);//主持人状态
                            courseLiveTeacher.setConfirm(CourseConfirmStatus.DQR);//待确认
                            courseLiveTeacherRepository.save(courseLiveTeacher);
                        }
                        if (controllerFlag) {
                            CourseLiveTeacher courseLiveTeacher = new CourseLiveTeacher();
                            courseLiveTeacher.setCourseLiveId(courseLive.getId());//直播id
                            courseLiveTeacher.setTeacherType(TeacherType.CK);//场控状态
                            courseLiveTeacher.setConfirm(CourseConfirmStatus.DQR);//待确认
                            courseLiveTeacherRepository.save(courseLiveTeacher);
                        }
                    }
                }
                status = CourseStatus.ZJAP;
            }else{  //否则 直接到完成
                status = CourseStatus.WC;
            }
            course.setStatus(status);//状态写入
        }
        courseRepository.save(course);
    }

    @Override
    @Transactional
	public void sendCourseLiveTeacherConfirmSms(Long id) {
		Course course = courseRepository.findOne(id);

		// 需要发送的数据<手机号，信息内容>
		Map<String, Object[]> datas = Maps.newHashMap();

		// 课程直播集合
		List<CourseLive> courseLives = course.getCourseLives();

		courseLives.forEach(courseLive -> {
		    if(null==courseLive.getSourceId()) {//不为滚动排课
                courseLive.getCourseLiveTeachers().forEach(courseLiveTeacher -> {  //遍历数据
                    if (TeacherType.JS.equals(courseLiveTeacher.getTeacherType()) &&CourseConfirmStatus.DQR.equals(courseLiveTeacher.getConfirm())) {
                        Teacher teacher = courseLiveTeacher.getTeacher();
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
                            Teacher lastTeacher = courseLiveTeacher.getLastTeacher();
                            //发送短信通知直播取消
                            StringBuffer sb = new StringBuffer();
                            sb.append("您好,");
                            sb.append(lastTeacher.getName());
                            sb.append("老师,\"");
                            sb.append(course.getName());
                            sb.append("\"课程中给您安排的\"");
                            sb.append(courseLive.getName());
                            sb.append("\"的课程安排已取消");
                            SmsUtil.sendSms(lastTeacher.getPhone(), sb.toString());
                            log.info("发送取消排课短信 : {} -> {}", lastTeacher.getPhone(), sb.toString());
                        }
                    }

                });
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
    @Transactional
	public void sendCourseLiveAssitantConfirmSms(Long id) {
		Course course = courseRepository.findOne(id);

		// 需要发送的数据<手机号，信息内容>
		Map<String, Object[]> datas = Maps.newHashMap();

		// 课程直播集合
		List<CourseLive> courseLives = course.getCourseLives();

		courseLives.forEach(courseLive -> {
		    if(null==courseLive.getSourceId()) {  //不为滚动排课
                for (CourseLiveTeacher courseLiveTeacher : courseLive.getCourseLiveTeachers()) { //取出直播中数据集合
                    //如果不是讲师类型 并且待确认
                    if (!TeacherType.JS.equals(courseLiveTeacher.getTeacherType()) && CourseConfirmStatus.DQR.equals(courseLiveTeacher.getConfirm())) {
                        Teacher teacher = courseLiveTeacher.getTeacher();
                        String phone = teacher.getPhone();
                        // 短信内容
                        Object[] infos = datas.get(phone);
                        if (infos == null) {
                            infos = new Object[8];
                            infos[0] = teacher.getName();
                            infos[1] = course.getName();
                            infos[2] = 1;
                            infos[3] = course.getCourseCategory().getText();//课程类型
                            infos[4] = courseLiveTeacher.getTeacherType().getText();//教师类型
                            infos[6] = teacher.getId();
                            infos[7] = teacher.getTeacherType();
                            datas.put(phone, infos);
                        } else {
                            infos[2] = (int) infos[2] + 1;
                        }
                        if(null!=courseLiveTeacher.getLastTeacherId()&&!courseLiveTeacher.getLastTeacherId().equals(courseLiveTeacher.getTeacherId())){  //有上一个教师情况 说明更换了教师  给原教师发送信息
                            Teacher lastTeacher = courseLiveTeacher.getLastTeacher();
                            //发送短信通知直播取消
                            StringBuffer sb = new StringBuffer();
                            sb.append("您好,");
                            sb.append(lastTeacher.getName());
                            sb.append("老师,\"");
                            sb.append(course.getName());
                            sb.append("\"课程中给您安排的\"");
                            sb.append(courseLive.getName());
                            sb.append("\"的课程安排已取消");
                            SmsUtil.sendSms(lastTeacher.getPhone(), sb.toString());
                            log.info("发送取消排课短信 : {} -> {}", lastTeacher.getPhone(), sb.toString());
                        }
                    }
                }
            }
		});

		// 发送信息
		datas.forEach((key, value) -> {
            ConfirmToken confirmToken = new ConfirmToken();
            confirmToken.setTeacherId((Long) value[6]);
            confirmToken.setSourseId(course.getId());
            confirmToken.setTeacherType((TeacherType) value[7]);
            confirmToken.setToken(UUID.randomUUID().toString().replaceAll("-", ""));

            confirmTokenRepository.save(confirmToken);

            value[5] = apiHost + "/confirm/confirm.html?token=" + confirmToken.getToken();

            String content = String.format(SMSTemplate.COURSE_CONFIRM, value);

            SmsUtil.sendSms(key, content);

            log.info("发送助教确认短信 : {} -> {}", key, content);
        });
	}

    @Override
    public void saveInterview(Long courseId, List<Long> teacherIds) {
        teacherIds.forEach(teacherId -> {
            courseRepository.saveInterviewTeacherId(courseId, teacherId);
        });
    }

    @Override
    public Boolean cancelCourse(Long courseId) {
        return 0 != courseRepository.updateCourseStatusById(courseId, CourseStatus.JSAP.ordinal(), CourseStatus.ZBAP.ordinal());
    }

    @Override
    public void findCourseStatusByCourseId(Long courseId) {
        CourseStatus status = courseRepository.findCourseStatusByCourseId(courseId);
        if (status != null && status.equals(CourseStatus.ZBAP)) {//如果直播安排状态 表示已经被运营撤销
            throw new BadRequestException("该课程已撤销,请勿操作");
        }
    }

    @Override
    @Transactional
    public void sendCourseDeleteSms(Course course, String reason) {
        CourseStatus status = course.getStatus();
        Set<String> phones = Sets.newHashSet();//存储手机号
        if (status.ordinal() >= 2) {//直播安排或教师安排直接删除
            course.getCourseLives().forEach(live -> {
                if(null==live.getSourceId()) {
                    live.getCourseLiveTeachers().forEach(lt -> {
                        Teacher teacher = lt.getTeacher();
                        if (teacher != null && CourseConfirmStatus.QR.equals(lt.getConfirm())&&null==lt.getSourceId()) {  //给已确认教师发送短信
                            phones.add(teacher.getPhone());//添加讲师电话
                        }
                    });
                }
            });
            if(phones!=null&&!phones.isEmpty()){
                List list = Lists.newArrayList();
                list.addAll(phones);
                SmsUtil.sendMultipleSms(list, "\"" + course.getName() + "\"该课程已删除,课程下相关安排已取消");
            }
        }
        DeleteCourseDetail detail = new DeleteCourseDetail();
        detail.setCourseId(course.getId());
        detail.setStatus(course.getStatus());
        detail.setReason(reason);
        detail.setCourseCategory(course.getCourseCategory());
        detail.setCourseName(course.getName());
        deleteCourseDetailRepository.save(detail);
        courseRepository.delete(course.getId());//删除课程
    }

    @Override
    public Page<Course> getVideoCourse(Pageable page) {
        Specification<Course> querySpecific = (root, criteriaQuery, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("courseCategory"), CourseCategory.VIDEO));//录播课
            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
        Sort sort = new Sort(Sort.Direction.DESC, "createdDate");
        Pageable pageable = new PageRequest(page.getPageNumber(), page.getPageSize(), sort);
        return courseRepository.findAll(querySpecific, pageable);
    }


}
