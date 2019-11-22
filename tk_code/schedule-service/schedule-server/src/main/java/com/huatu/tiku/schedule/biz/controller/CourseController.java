package com.huatu.tiku.schedule.biz.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import com.huatu.tiku.schedule.base.config.CustomUser;
import com.huatu.tiku.schedule.biz.domain.Role;
import com.huatu.tiku.schedule.biz.dto.DeleteCourseDto;
import com.huatu.tiku.schedule.biz.dto.IdDto;
import com.huatu.tiku.schedule.biz.enums.CourseCategory;
import com.huatu.tiku.schedule.biz.enums.CourseStatus;
import com.huatu.tiku.schedule.biz.enums.ExamType;

import com.huatu.tiku.schedule.biz.service.CourseLiveTeacherService;
import com.huatu.tiku.schedule.biz.service.TeacherService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.huatu.tiku.schedule.base.exception.BadRequestException;
import com.huatu.tiku.schedule.biz.domain.Course;
import com.huatu.tiku.schedule.biz.dto.CreateCourseDto;
import com.huatu.tiku.schedule.biz.service.CourseLiveService;
import com.huatu.tiku.schedule.biz.service.CourseService;

import java.util.*;

/**
 * 课程Controller
 *
 * @author Geek-S
 */
@RestController
@RequestMapping("course")
@Slf4j
public class CourseController {

    private final CourseService courseService;

    private final CourseLiveService courseLiveService;

    private final CourseLiveTeacherService courseLiveTeacherService;

    private final TeacherService teacherService;

    @Autowired
    public CourseController(CourseService courseService,
                            CourseLiveService courseLiveService, CourseLiveTeacherService courseLiveTeacherService, TeacherService teacherService) {
        this.courseService = courseService;
        this.courseLiveService = courseLiveService;
        this.courseLiveTeacherService = courseLiveTeacherService;
        this.teacherService = teacherService;
    }

    /**
     * 新增课程
     *
     * @param courseDto 课程
     * @return 课程
     */
    @PostMapping("createCourse")
    public Course createCourse(@Valid @RequestBody CreateCourseDto courseDto, BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        // 转成domain并入库
        Course course = new Course();
        BeanUtils.copyProperties(courseDto, course);
        CourseCategory courseCategory = course.getCourseCategory();
        if(null!=courseCategory&&CourseCategory.VIDEO.equals(courseCategory)){
            course.setStatus(CourseStatus.WC);//录播课直接完成状态
            course.setAssistantFlag(false);
            course.setCompereFlag(false);
            course.setControllerFlag(false);
            course.setSchoolType(null);
            course.setSatFlag(false);
            course.setSunFlag(false);
            course.setPlace(null);//录播地点为空
            courseService.save(course);
            return  course;
        }else {
            if(CourseCategory.LIVE.equals(courseCategory)){
                course.setPlace(null);//直播地点为空
            }
            course.setStatus(CourseStatus.ZBAP);
            courseService.save(course);
            if (course.getExamType().equals(ExamType.MS) && courseDto.getTeacherIds() != null) {//面试类型 并选择了推荐教师
                courseService.saveInterview(course.getId(), courseDto.getTeacherIds());
            }
            // 创建默认直播
            Calendar dateBegin = Calendar.getInstance();
            dateBegin.setTime(course.getDateBegin());
            Date dateEnd = course.getDateEnd();
            List<Date> dates = Lists.newArrayList();
            Boolean falg = true;
            while (dateBegin.getTime().getTime() <= dateEnd.getTime()) {
                // 校验周六日上课
//			if((dateBegin.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY)&&(dateBegin.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY)){
                if (!((dateBegin.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY && !course.getSatFlag())
                        || (dateBegin.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY && !course.getSunFlag()))) {
                    if (course.getSeparatorFlag()) {//选择隔天上课
                        if (falg) {//标志正确 添加日期 并改变标志位 使下一次判断失效
                            dates.add(dateBegin.getTime());
                            falg = false;
                        } else {//标志不正确 不添加 重置标志
                            falg = true;
                        }
                    } else {//没选隔天上课 直接添加日期
                        dates.add(dateBegin.getTime());
                    }
                } else {//不是周末重置标志
                    falg = true;
                }
                dateBegin.add(Calendar.DAY_OF_YEAR, 1);
            }

            //  默认时间（可以在创建课程中通过参数传入）
            List<List<String>> defaultTimes = Collections.singletonList(Lists.newArrayList("19:00", "21:30"));

            courseLiveService.createCourseLive(course.getId(), course.getSubjectId(), dates, defaultTimes, false);

            return course;
        }
    }

    /**
     * 条件查询课程（助教安排）
     *
     * @param examType     考试类型
     * @param name         名称
     * @param id           课程id
     * @param subjectId    科目id
     * @param dateBegin    范围起始日期
     * @param dateEnd      范围结束日期
     * @param teacherName  教师姓名
     * @param courseStatus 课程状态
     * @param page         分页
     * @return 课程列表
     */
    @GetMapping("findCoursesZJ")
    public Page<Course> findCoursesZJ(ExamType examType, String name, Long id, Long subjectId,
                                      @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateBegin,
                                      @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateEnd,String teacherName, CourseStatus courseStatus,
                                      Pageable page) {
        return courseService.getCourseListZJ(examType, name, id, subjectId, dateBegin, dateEnd, teacherName, courseStatus,
                page);
    }

    /**
     * 条件查询课程
     *
     * @param category     课程类型
     * @param examType     考试类型
     * @param name         名称
     * @param id           课程id
     * @param subjectId    科目id
     * @param dateBegin    范围起始日期
     * @param dateEnd      范围结束日期
     * @param teacherName  教师姓名
     * @param courseStatus 课程状态
     * @param page         分页
     * @return 课程列表
     */
    @GetMapping("findCourses")
    public Page<Course> findCourses(CourseCategory category,ExamType examType, String name, Long id, Long subjectId,
                                    @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateBegin,
                                    @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateEnd, String teacherName, CourseStatus courseStatus,
                                    Pageable page, @AuthenticationPrincipal CustomUser user) {
        // 考试类型
        List<ExamType> examTypes = Lists.newArrayList();

        // 可以查看全部的角色
        List<String> roleNames = Lists.newArrayList("超级管理员", "人力","教学管理组");

        // 当前用户角色
        Set<Role> roles = user.getRoles();

        // 管理员
        Optional<Role> adminFlag = roles.stream().filter(role -> roleNames.contains(role.getName())).findFirst();

        if (adminFlag.isPresent()) {
            if (examType != null) {
                examTypes.add(examType);
            }
        } else {
            // 教务
            Optional<Role> jwFlag = roles.stream().filter(role -> role.getName().equals("教务")||role.getName().equals("录播教务")).findFirst();

            if (jwFlag.isPresent()) {
                // 数据权限
                Set<ExamType> dataPermissioins = user.getDataPermissions();

                if (examType == null) {
                    examTypes.addAll(dataPermissioins);
                } else {
                    // 如果传入考试类型，判断考试类型是否在权限内
                    if (dataPermissioins.contains(examType)) {
                        examTypes.add(examType);
                    } else {
                        throw new BadRequestException("无【" + examType.getText() + "】的查看权限");
                    }
                }
            } else {
                // 运营
                Optional<Role> operationFlag = roles.stream().filter(role -> role.getName().equals("运营") || role.getName().equals("录播产品")).findFirst();

                if (operationFlag.isPresent()) {
                    // 数据权限
                    Set<ExamType> dataPermissioins = user.getDataPermissions();

                    if (examType == null) {
                        examTypes.addAll(dataPermissioins);
                    } else {
                        // 如果传入考试类型，判断考试类型是否在权限内
                        if (dataPermissioins.contains(examType)) {
                            examTypes.add(examType);
                        } else {
                            throw new BadRequestException("无【" + examType.getText() + "】的查看权限");
                        }
                    }
                } else {
                    throw new BadRequestException("无课程的查看权限");
                }
            }
        }

        List<CourseCategory> list=Lists.newArrayList();
        if (!adminFlag.isPresent() && examTypes.isEmpty()) {
            throw new BadRequestException("无课程的查看权限");
        }
        if(null!=category){
            list.add(category);
        }else{
            if (!adminFlag.isPresent()) {  //不是指定角色 判断权限
                Optional<Role> jwFlag = roles.stream().filter(role -> role.getName().equals("教务")||role.getName().equals("运营")).findFirst();
                if (jwFlag.isPresent()) { // 教务或运营
                    list.add(CourseCategory.LIVE);
                    list.add(CourseCategory.XXK);

                }
                Optional<Role> lbFlag = roles.stream().filter(role -> role.getName().equals("录播教务")||role.getName().equals("录播产品")).findFirst();
                if (lbFlag.isPresent()) { //录播
                    list.add(CourseCategory.VIDEO);
                }
            }
        }
        return courseService.getCourseList(list,examTypes, name, id, subjectId, dateBegin, dateEnd, teacherName,
                courseStatus, page);
    }

    /**
     * 教务查询课程  TODO 该接口未使用 待确认后删除
     */
    @GetMapping("findCourseByPermission")
    public Page<Course> findCourseByPermission(CourseCategory category,
                                               ExamType examType,
                                               String name,
                                               Long id,
                                               Long subjectId,
                                               @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateBegin,
                                               @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateEnd,
                                               String teacherName,
                                               CourseStatus courseStatus,
                                               Pageable page,
                                               @AuthenticationPrincipal CustomUser user) {

        // 考试类型
        List<ExamType> examTypes = Lists.newArrayList();
        // 可以查看全部的角色
        List<String> roleNames = Lists.newArrayList("超级管理员");
        // 当前用户角色
        Set<Role> roles = user.getRoles();
        // 管理员
        Optional<Role> adminFlag = roles.stream().filter(role -> roleNames.contains(role.getName())).findFirst();
        if (!adminFlag.isPresent()) {//非管理员
            // 教务
            Optional<Role> jwFlag = roles.stream().filter(role -> role.getName().equals("教务")||role.getName().equals("录播教务")).findFirst();
            if (jwFlag.isPresent()) {//教务
                // 数据权限
                Set<ExamType> dataPermissioins = user.getDataPermissions();
                if (examType == null) {//没传参数把权限传进去
                    examTypes.addAll(dataPermissioins);
                } else {
                    // 如果传入考试类型，判断考试类型是否在权限内
                    if (dataPermissioins.contains(examType)) {
                        examTypes.add(examType);
                    } else {
                        throw new BadRequestException("无【" + examType.getText() + "】的查看权限");
                    }
                }
            } else {
                throw new BadRequestException("无查看权限");
            }
        } else {//管理员 将全部考试类型添加
            if (examType == null) {
                examTypes.addAll(Arrays.asList(ExamType.values()));
            } else {
                examTypes.add(examType);
            }
        }

        if (!adminFlag.isPresent() && examTypes.isEmpty()) {
            throw new BadRequestException("无课程的查看权限");
        }

        return courseService.getCourseList(category,examTypes, name, id, subjectId, dateBegin, dateEnd, teacherName, courseStatus, page);

    }

    /**
     * 提交直播安排
     *
     * @param idDto 课程ID
     * @return 结果
     */
    @PostMapping("submitCourseLive")
    public Map<String, Object> submitCourseLive(@Valid @RequestBody IdDto idDto, BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException("课程" + bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return courseService.submitCourseLive(idDto.getId());
    }

    /**
     * 提交教师安排
     *
     * @param idDto 课程ID
     * @return 结果
     */
    @PostMapping("submitCourseLiveTeacher")
    public Map<String, Object> submitCourseLiveTeacher(@Valid @RequestBody IdDto idDto, BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException("课程" + bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        Course course = courseService.findOne(idDto.getId());
        Map<String, Object> result = courseService.submitCourseLiveTeacher(course);

        if (result.get("success").equals(true)) {
            // 发送短信
            try {
                courseService.sendCourseLiveTeacherConfirmSms(idDto.getId());
            } catch (Exception e) {
                log.error("发送教师确认短信异常", e);
            }
        }

        return result;
    }

    /**
     * 提交助教安排
     *
     * @param idDto 课程ID
     * @return 结果
     */
    @PostMapping("submitCourseLiveAssitant")
    public Map<String, Object> submitCourseLiveAssitant(@Valid @RequestBody IdDto idDto, BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException("课程" + bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        Map<String, Object> result = courseService.submitCourseLiveAssitant(idDto.getId());

        if (result.get("success").equals(true)) {
            // 发送短信
            try {
                courseService.sendCourseLiveAssitantConfirmSms(idDto.getId());
            } catch (Exception e) {
                log.error("发送助教确认短信异常", e);
            }
        }

        return result;
    }

    /**
     * 滚动排课课程列表
     *
     * @param id    课程id
     * @param dates 日期
     * @return 课程列表
     */
    @GetMapping("rollingCourse")
    public List<Map<String, Object>> rollingCourse(Long id, @DateTimeFormat(pattern = "yyyy-MM-dd") Date[] dates) {
        if (id == null) {
            throw new BadRequestException("课程ID不能为空");
        }

        if (dates == null || dates.length == 0) {
            throw new BadRequestException("日期不能为空");
        }

        List<Map<String, Object>> result = Lists.newArrayList();

        courseService.rollingCourse(id, Arrays.asList(dates)).forEach(course -> {
            result.add(ImmutableMap.of("id", course.getId(), "name", course.getName()));
        });

        return result;
    }

    @GetMapping("authDeleteCourse")
    public void auth(@AuthenticationPrincipal CustomUser user,Long courseId) {
        Course course = courseService.findOne(courseId);
        CourseStatus status = course.getStatus();
        if (!CourseStatus.ZBAP.equals(status)) {  //不是课程直播 判断权限
            Set<Role> roles = user.getRoles(); //当前用户权限
            if (!roles.stream().filter(role -> role.getName().equals("超级管理员")).findFirst().isPresent()) { //不是管理员
                // 教务
                Optional<Role> jwFlag = roles.stream().filter(role -> role.getName().equals("教务")
                        ||role.getName().equals("录播教务")).findFirst();
                if (jwFlag.isPresent()) {//教务
                    // 数据权限
                    Set<ExamType> dataPermissioins = user.getDataPermissions();
                    ExamType examType = course.getExamType();
                    // 判断删除的课程类型是否在权限内
                    if (!dataPermissioins.contains(examType)) {
                        throw new BadRequestException("无【" + examType.getText() + "】的删除权限");
                    }
                } else {//非管理员非教务
                    throw new BadRequestException("该课程已进入排课流程，不能删除，请联系教务或管理员");
                }
            }
        }
    }

    /**
     * 删除课程
     *
     * @param dto 参数
     * @return 删除结果
     */
    @PostMapping("deleteCourse")
    public Boolean deleteCourse(@Valid @RequestBody DeleteCourseDto dto, BindingResult bindingResult,
                                HttpServletRequest request, @AuthenticationPrincipal CustomUser user) {
        //		// 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException("课程直播" + bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        String phone = user.getPhone();
        HttpSession session = request.getSession();
        String code = (String) session.getAttribute(phone);//验证码
        if (!dto.getCode().equals(code)) {
            throw new BadRequestException("验证码错误");
        }
        session.removeAttribute(phone);
        Long courseId = dto.getCourseId();//课程id
        Course course = courseService.findOne(courseId);
        if (course == null) {
            throw new BadRequestException("该课程已删除");
        }
        ExamType examType = course.getExamType();//课程类型

        // 可以查看全部的角色
        List<String> roleNames = Lists.newArrayList("超级管理员");
        // 当前用户角色
        Set<Role> roles = user.getRoles();
        // 管理员
        Optional<Role> adminFlag = roles.stream().filter(role -> roleNames.contains(role.getName())).findFirst();
        if (!adminFlag.isPresent()) {//非管理员
            // 教务
            Optional<Role> jwFlag = roles.stream().filter(role -> role.getName().equals("教务")
                    ||role.getName().equals("录播教务")).findFirst();
            if (jwFlag.isPresent()) {//教务
                // 数据权限
                Set<ExamType> dataPermissioins = user.getDataPermissions();
                // 判断删除的课程类型是否在权限内
                if (!dataPermissioins.contains(examType)) {
                    throw new BadRequestException("无【" + examType.getText() + "】的删除权限");
                }
            } else {//非管理员非教务直接报错
                throw new BadRequestException("无删除课程权限");
            }
        }
        courseService.sendCourseDeleteSms(course, dto.getReason());//删除课程
        return true;
    }

    /**
     * 运营撤销课程(将教师安排状态改到直播安排状态)
     *
     * @param idsDto 课程id
     * @return 结果
     */
    @PostMapping("courseCancel")
    public Boolean cancelCourse(@Valid @RequestBody IdDto idsDto, BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException("课程直播" + bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        return courseService.cancelCourse(idsDto.getId());
    }

    @GetMapping("deleteAdmin")
    public  void deleteCourse(Long courseId){
        courseService.delete(courseId);
    }

    @GetMapping("getVideoCourse")
    public Page<Course> getVideoCourse(Pageable page){
        return courseService.getVideoCourse( page);
    }

    @GetMapping("deleteLiveAdmin")
    public  void deleteCourseLive(Long id){
        courseLiveService.delete(id);
    }

    @GetMapping("deleteLiveTeacherAdmin")
    public  void deleteCourseLiveTeacher(Long id){
        courseLiveTeacherService.delete(id);
    }

    @GetMapping("deleteTeacher")
    public  void deleteTeacher(Long id){
        teacherService.delete(id);
    }

}

