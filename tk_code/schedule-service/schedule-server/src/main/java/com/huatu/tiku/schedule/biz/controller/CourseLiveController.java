package com.huatu.tiku.schedule.biz.controller;

import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

import javax.validation.Valid;

import com.google.common.collect.Sets;
import com.huatu.tiku.schedule.biz.domain.*;
import com.huatu.tiku.schedule.biz.dto.*;
import com.huatu.tiku.schedule.biz.enums.*;
import com.huatu.tiku.schedule.biz.service.*;
import com.huatu.tiku.schedule.biz.util.*;
import com.huatu.tiku.schedule.biz.vo.*;
import com.huatu.tiku.schedule.biz.vo.CourseInfoPackage.CourseInfoVo;
import com.huatu.tiku.schedule.biz.vo.Schedule.PageVo;
import com.huatu.tiku.schedule.biz.vo.Schedule.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.schedule.base.config.CustomUser;
import com.huatu.tiku.schedule.base.exception.BadRequestException;
import org.springframework.web.multipart.MultipartFile;

/**
 * 课程直播Controller
 *
 * @author Geek-S
 */
@RestController
@RequestMapping("courseLive")
public class CourseLiveController {

    private final CourseService courseService;

    private final CourseLiveService courseLiveService;

    private final CourseLiveTeacherService courseLiveTeacherService;


    @Autowired
    public CourseLiveController(CourseService courseService, CourseLiveService courseLiveService,
                                CourseLiveTeacherService courseLiveTeacherServiceService) {
        this.courseService = courseService;
        this.courseLiveService = courseLiveService;
        this.courseLiveTeacherService = courseLiveTeacherServiceService;
    }

    @GetMapping("getCourseInfo")
    public Object getCourseInfo(Long courseId) {
        if (courseId == null) {
            throw new BadRequestException("课程ID不能为空");
        }
        Course course = courseService.findOne(courseId);//数据库查询课程数据
        if (course == null) {
            throw new BadRequestException("课程不存在，课程ID [" + courseId + "]");
        }
        List<CourseLive> courseLives = course.getCourseLives();//取出直播集合
        //对直播日期排序
        courseLives.sort((o1,o2)->{
            int result=o1.getDateInt()-o2.getDateInt();//日期排序
            result=result==0?o1.getTimeBegin()-o2.getTimeBegin():result;//开始时间排序
            result=result==0?o1.getTimeEnd()-o2.getTimeEnd():result;//结束时间排序
            result=result==0?o1.getId().intValue()-o2.getId().intValue():result;
            return result;
        });
        return new CourseInfoVo(course);
    }

    /**
     * 新增课程直播
     */
    @PostMapping("createCourseLive")
    public List<CourseLive> createCourseLive(@Valid @RequestBody CreateCourseLiveDto courseLiveDto,
                                             BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return courseLiveService.createCourseLive(courseLiveDto.getCourseId(), null, courseLiveDto.getDates(), courseLiveDto.getTimes(), courseLiveDto.getToken());
    }

    /**
     * 添加课程状态判断 直播安排状态抛出异常
     * 新增课程直播
     */
    @PostMapping("createCourseLiveByStatus")
    public List<CourseLive> createCourseLiveByStatus(@Valid @RequestBody CreateCourseLiveDto courseLiveDto,
                                                     BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        courseService.findCourseStatusByCourseId(courseLiveDto.getCourseId());//如果为直播安排状态会抛出异常
        return courseLiveService.createCourseLive(courseLiveDto.getCourseId(), null, courseLiveDto.getDates(), courseLiveDto.getTimes(), courseLiveDto.getToken());
    }

    /**
     * 删除课程直播
     *
     * @param dto 课程直播ID/日期
     * @return 操作结果
     */
    @PostMapping("deleteCourseLiveBatch")
    public Boolean deleteCourseLiveBatch(@Valid @RequestBody DeleteCourseLiveBatchDto dto,
                                         BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        List<Long> courseLiveIds = dto.getCourseLiveIds();
        List<Long> longs = courseLiveService.findBySourceIdIn(courseLiveIds);
        courseLiveService.delete(longs);
        courseLiveService.delete(courseLiveIds);
        return true;
    }

    /**
     * 删除课程直播
     * 根据课程ID和日期删除课程直播
     *
     * @param dto 课程直播ID/日期
     * @return 操作结果
     */
    @PostMapping("deleteCourseLiveBatchByStatus")
    public Boolean deleteCourseLiveBatchByStatus(@Valid @RequestBody DeleteCourseLiveBatchDto dto,
                                                 BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        courseService.findCourseStatusByCourseId(dto.getCourseId());//如果为直播安排状态会抛出异常

        List<Long> courseLiveIds = dto.getCourseLiveIds();
        List<Long> longs = courseLiveService.findBySourceIdIn(courseLiveIds);
        courseLiveService.delete(longs);
        courseLiveService.delete(courseLiveIds);
        return true;
    }

    /**
     * 获取教师课表
     *
     * @param dateBegin 日期开始
     * @param dateEnd   日期结束
     * @param teacherId 教师id
     * @return 课程直播
     */
    @GetMapping("schedule")
    public PageVo schedule(@DateTimeFormat(pattern = "yyyy-MM-dd") Date dateBegin,
                           @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateEnd, Long teacherId,
                           ExamType examType,Long subjectId,
                           CourseCategory category,
                           @AuthenticationPrincipal CustomUser user, Pageable page) {
        if (dateBegin == null) {
            throw new BadRequestException("日期不能为空");
        }
        if (dateEnd == null) {//没选结束时间 默认结束时间为当前时间
            dateEnd = new Date();
        }
        if (dateBegin.after(dateEnd)) {
            throw new BadRequestException("开始时间不能晚于结束时间");
        }
        // 考试类型
        List<ExamType> examTypes = Lists.newArrayList();
        List<CourseCategory> categorys = Lists.newArrayList();
        // 可以查看全部的角色
        List<String> roleNames = Lists.newArrayList("超级管理员", "人力","教学管理组");
        // 当前用户角色
        Set<Role> roles = user.getRoles();
        // 管理员
        Optional<Role> adminFlag = roles.stream().filter(role -> roleNames.contains(role.getName())).findFirst();
        if (!adminFlag.isPresent()) { //不是指定角色 判断权限
            Optional<Role> jwFlag = roles.stream().filter(role -> role.getName().equals("教务")|| role.getName().equals("运营")|| role.getName().equals("录播教务")|| role.getName().equals("录播产品")).findFirst();
            if (jwFlag.isPresent()) { // 教务
                // 数据权限
                Set<ExamType> dataPermissioins = user.getDataPermissions();
                if (teacherId == null) { //没选教师情况
                    if(examType==null){
                        examTypes.addAll(dataPermissioins);
                    }else{
                        examTypes.add(examType);
                    }
                }
                if (null != category) {
                    categorys.add(category);
                } else {
                    if(roles.stream().filter(role -> role.getName().equals("教务")|| role.getName().equals("运营")).findFirst().isPresent()){
                        categorys.add(CourseCategory.LIVE);
                        categorys.add(CourseCategory.XXK);
                    }
                    if(roles.stream().filter(role -> role.getName().equals("录播教务")|| role.getName().equals("录播产品")).findFirst().isPresent()){
                        categorys.add(CourseCategory.VIDEO);
                    }
                }
            } else { //不是教务判断是否是组长
                Optional<Role> zzFlag = roles.stream().filter(role -> role.getName().equals("组长")).findFirst();
                Boolean leaderFlag = user.getLeaderFlag();
                if (leaderFlag&&zzFlag.isPresent()) { //组长
                    if (teacherId == null) { //没选教师情况
                        if(examType==null){
                            examTypes.add(user.getExamType());
                        }else{
                            examTypes.add(examType);
                        }
                        if(subjectId==null){
                            subjectId=user.getSubjectId();
                        }
                        if (null != category) {
                            categorys.add(category);
                        } else {
                            categorys = null;
                        }
                    }  //未勾选教师 //不做处理 符合要求

                } else { //不是教务 不是组长报错
                    throw new BadRequestException("无讲师课表的查看权限");
                }
            }
        } else {  //指定角色
            if (null != examType) { //指定类型添加类型 不指定类型添加全部
                examTypes.add(examType);
            } else {  //不指定类型添加全部
                examTypes = null;  //设置为null 即可查询全部数据
            }
            if (null != category) {
                categorys.add(category);
            } else {
                categorys = null;
            }
        }

        if(teacherId!=null){  //如果勾选了教师 清空其他条件
            examTypes=null;
            subjectId=null;
        }

        // 组装表头
        Calendar start = Calendar.getInstance();
        start.setTime(dateBegin);

        Page<CourseLiveScheduleVo> courseLiveScheduleVos = courseLiveService.schedule(examTypes, subjectId, dateBegin,
                dateEnd, teacherId, page,categorys);
        List<Object> result = new ArrayList<>();
        String dateString = null;//临时字符串 记录日期
        for (CourseLiveScheduleVo vo : courseLiveScheduleVos.getContent()) {
            if (!vo.getDate().equals(dateString)) {//不相等添加日期
                dateString = vo.getDate();
                result.add(ImmutableMap.of("date", dateString));
            }
            Schedule schedule = new Schedule();
            schedule.setTime(vo.getTimeBegin() + "-" + vo.getTimeEnd());//时间
            schedule.setCourseName(vo.getCourseName());//课程名
            schedule.setLiveName(vo.getCourseLiveName());//直播名
            schedule.setCategoryName(vo.getCategoryName());//课程类型
            schedule.setVideoRoomId(vo.getVideoRoomId());
            schedule.setVideoRoomName(vo.getVideoRoomName());
            List<CourseLiveScheduleVo.TeacherInfo> teacherInfos = vo.getTeacherInfos();
            if (teacherInfos != null && !teacherInfos.isEmpty()) {
                StringBuffer sbTeacher = new StringBuffer();
                StringBuffer sbAss = new StringBuffer();
                StringBuffer sbCom = new StringBuffer();
                StringBuffer sbCtrl = new StringBuffer();
                StringBuffer sbSys = new StringBuffer();
                StringBuffer sbZks = new StringBuffer();
                StringBuffer sbSubject = new StringBuffer();
                Set<String> subjectSet= Sets.newHashSet();
                teacherInfos.forEach(info -> {
                    switch (info.getType()){
                        case JS:
                            if(info.getName()!=null){
                                sbTeacher.append(info.getName());
                                sbTeacher.append(",");
                            }
                            if (info.getSubject() != null ) {
                                subjectSet.add(info.getSubject());
                            }
                            break;
                        case ZCR:
                            if(info.getName()!=null) {
                                sbCom.append(info.getName());
                                sbCom.append(",");
                            }
                            break;
                        case ZJ:
                            if(info.getName()!=null){
                                sbAss.append(info.getName());
                                sbAss.append(",");
                            }
                            break;
                        case CK:
                            if(info.getName()!=null) {
                                sbCtrl.append(info.getName());
                                sbCtrl.append(",");
                            }
                            break;
                        case XXS:
                            break;
                        case SYS:
                            if(info.getName()!=null) {
                                sbSys.append(info.getName());
                                sbSys.append(",");
                            }
                            break;
                        case ZKS:
                            if(info.getName()!=null) {
                                sbZks.append(info.getName());
                                sbZks.append(",");
                            }
                            break;
                    }
                });
                if (!subjectSet.isEmpty() ) {//有科目
                    subjectSet.forEach(subjectString->{
                        sbSubject.append(subjectString);
                        sbSubject.append(",");
                    });
                }
                int length = sbTeacher.length();
                if(length!=0){
                    sbTeacher.deleteCharAt(length - 1);
                    schedule.setTeacherNames(sbTeacher.toString());//教师名
                }
                int lengthCom = sbCom.length();
                if(lengthCom!=0){
                    sbCom.deleteCharAt(lengthCom - 1);
                    schedule.setCompereName(sbCom.toString());//主持人
                }
                int lengthAss = sbAss.length();
                if(lengthAss!=0){
                    sbAss.deleteCharAt(lengthAss - 1);
                    schedule.setAssistantName(sbAss.toString());//助教
                }
                int lengthCtrl = sbCtrl.length();
                if(lengthCtrl!=0){
                    sbCtrl.deleteCharAt(lengthCtrl - 1);
                    schedule.setCtrlName(sbCtrl.toString());//场控
                }
                int lengthSys = sbSys.length();
                if(lengthSys!=0){
                    sbSys.deleteCharAt(lengthSys - 1);
                    schedule.setSysName(sbSys.toString());//摄影师
                }
                int lengthZks = sbZks.length();
                if(lengthZks!=0){
                    sbZks.deleteCharAt(lengthZks - 1);
                    schedule.setZksName(sbZks.toString());//质控师
                }

                int lengthSubject = sbSubject.length();
                if(lengthSubject!=0){
                    sbSubject.deleteCharAt(lengthSubject - 1);
                    schedule.setExamtypeSubject(sbSubject.toString());//科目名
                }
            }
            result.add(schedule);
        }
        PageVo pageVo = new PageVo();
        pageVo.setContent(result);
        pageVo.setFirst(courseLiveScheduleVos.isFirst());
        pageVo.setLast(courseLiveScheduleVos.isLast());
        pageVo.setNumber(courseLiveScheduleVos.getNumber());
        pageVo.setNumberOfElements(courseLiveScheduleVos.getNumberOfElements());
        pageVo.setSize(courseLiveScheduleVos.getSize());
        pageVo.setTotalElements(courseLiveScheduleVos.getTotalElements());
        pageVo.setTotalPages(courseLiveScheduleVos.getTotalPages());
        return pageVo;
    }

    @GetMapping("teacherSchedule")
    public Map<String, Object> teacherSchedule(Long teacherId,
                                  @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateBegin,
                                  @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateEnd,
                                               @AuthenticationPrincipal CustomUser user){

        if(teacherId==null){
            throw new BadRequestException("请选择指定教师");
        }
        if (dateBegin == null) {
            throw new BadRequestException("日期不能为空");
        }
        if (dateEnd == null) {//没选结束时间 默认结束时间为当前时间
            dateEnd = new Date();
        }
        if (dateBegin.after(dateEnd)) {
            throw new BadRequestException("开始时间不能晚于结束时间");
        }

//        // 可以查看全部的角色
//        List<String> roleNames = Lists.newArrayList("超级管理员", "人力","教学管理组");
//        // 当前用户角色
//        Set<Role> roles = user.getRoles();
//        // 管理员
//        Optional<Role> adminFlag = roles.stream().filter(role -> roleNames.contains(role.getName())).findFirst();
//        if (!adminFlag.isPresent()) { //不是指定角色 判断权限
//            Optional<Role> jwFlag = roles.stream().filter(role -> role.getName().equals("教务")).findFirst();
//            if (jwFlag.isPresent()) { // 教务
//                // 数据权限
//                Set<ExamType> dataPermissioins = user.getDataPermissions();
//                if(examType==null){
//                    examTypes.addAll(dataPermissioins);
//                }else{
//                    examTypes.add(examType);
//                }  //选了教师 //不做处理 类型科目都符合要求 不做处理
//
//            } else { //不是教务判断是否是组长
//                Optional<Role> zzFlag = roles.stream().filter(role -> role.getName().equals("组长")).findFirst();
//                Boolean leaderFlag = user.getLeaderFlag();
//                if (leaderFlag&&zzFlag.isPresent()) { //组长
//                    if (teacherId == null) { //没选教师情况
//                        if(examType==null){
//                            examTypes.add(user.getExamType());
//                        }else{
//                            examTypes.add(examType);
//                        }
//                        if(subjectId==null){
//                            subjectId=user.getSubjectId();
//                        }
//                    }  //未勾选教师 //不做处理 符合要求
//
//
//                } else { //不是教务 不是组长报错
//                    throw new BadRequestException("无讲师课表的查看权限");
//                }
//            }
//        }
        // 组装表头
        Calendar start = Calendar.getInstance();
        start.setTime(dateBegin);

        List<String> headers = Lists.newArrayList();

        for (; start.getTime().before(dateEnd); start.add(Calendar.DAY_OF_YEAR, 1)) {
            headers.add(DateformatUtil.format0(start.getTime()));
        }

        headers.add(DateformatUtil.format0(start.getTime()));

        List<CourseLiveScheduleVo> courseLiveScheduleVos = courseLiveService.mySchedule(null, null, dateBegin,
                dateEnd, null, null, teacherId);

        List<List<CourseLiveScheduleVo>> datas = Lists.newArrayList();

        IntStream.range(0, headers.size()).forEach(i ->
                datas.add(null)
        );

        // 表头放入Map
        Map<String, Integer> dateDic = Maps.newHashMap();

        for (int i = 0; i < headers.size(); i++) {
            dateDic.put(headers.get(i), i);
        }

        // 将数据放入对应日期
        courseLiveScheduleVos.forEach(courseLiveScheduleVo -> {
            Integer index = dateDic.get(courseLiveScheduleVo.getDate());

            List<CourseLiveScheduleVo> items = datas.get(index);

            if (items == null) {
                items = Lists.newArrayList();
                datas.set(index, items);
            }

            items.add(courseLiveScheduleVo);
        });

        return ImmutableMap.of("datas", datas);
    }

    /**
     * 滚动排课查询
     *
     * @param currentCourseId 当前课程ID
     * @param dates           日期
     * @param courseId        课程ID
     * @param examType        考试类型
     * @param subjectId       科目ID
     * @return List
     */
    @GetMapping("findForRolling")
    public List<CourseLiveRollingVo> findForRolling(Long currentCourseId, @DateTimeFormat(pattern = "yyyy-MM-dd") Date[] dates, Long courseId,
                                                    ExamType examType, Long subjectId) {
        if (currentCourseId == null) {
            throw new BadRequestException("当前课程ID不能为空");
        }

        if (dates == null || dates.length == 0) {
            throw new BadRequestException("日期不能为空");
        }

        return courseLiveService.findForRolling(currentCourseId, Arrays.asList(dates), courseId, examType, subjectId);
    }

    /**
     * 提交滚动排课
     *
     * @param rollingScheduleDto 滚动排课参数
     */
    @PostMapping("rollingSchedule")
    public Boolean rollingSchedule(@Valid @RequestBody RollingScheduleDto rollingScheduleDto,
                                   BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        courseService.findCourseStatusByCourseId(rollingScheduleDto.getCourseId());//如果为直播安排状态会抛出异常

        courseLiveService.rollingSchedule(rollingScheduleDto.getCourseId(), rollingScheduleDto.getCourseLiveIds());

        return true;
    }

    /**
     * 修改直播名字 添加课程状态判断 直播安排状态抛出异常
     *
     * @param updateLiveNameDto 直播id 直播名
     * @return result
     */
    @PostMapping("updateLiveName")
    public Boolean updateLiveName(@Valid @RequestBody UpdateLiveNameDto updateLiveNameDto,
                                  BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        return 0 != courseLiveService.updateLiveName(updateLiveNameDto.getLiveId(), updateLiveNameDto.getLiveName());
    }

    /**
     * 一键排课
     *
     * @param dto 课程ID 日期
     * @return 结果
     */
    @PostMapping("oneKeySchedule")
    public Boolean oneKeySchedule(@Valid @RequestBody OneKeyScheduleDto dto, BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        courseLiveService.oneKeySchedule(dto.getCourseLiveIds());

        return true;
    }

    /**
     * 一键排课 助教版
     *
     * @param dto 课程ID 日期
     * @return 结果
     */
    @PostMapping("oneKeyScheduleAssistant")
    public Boolean oneKeyScheduleAssistant(@Valid @RequestBody OneKeyScheduleDto dto, BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        courseLiveService.oneKeyScheduleAssistant(dto.getCourseLiveIds());

        return true;
    }

    /**
     * 获取我的课表
     *
     * @param examType   考试类型
     * @param subjectId  科目ID
     * @param dateBegin  日期开始
     * @param dateEnd    日期结束
     * @param courseName 课程名称
     * @param liveRoomId 直播间ID
     * @return 课程直播
     */
    @GetMapping("mySchedule")
    public Map<String, Object> mySchedule(ExamType examType, Long subjectId,
                                          @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateBegin,
                                          @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateEnd, String courseName, Long liveRoomId,
                                          @AuthenticationPrincipal CustomUser user) {
        if (dateBegin == null) {
            throw new BadRequestException("日期不能为空");
        }
        if (dateEnd == null) {//没选结束时间 默认结束时间为当前时间
            dateEnd = new Date();
        }
        if (dateBegin.after(dateEnd)) {
            throw new BadRequestException("开始时间不能晚于结束时间");
        }

        // 组装表头
        Calendar start = Calendar.getInstance();
        start.setTime(dateBegin);

        List<String> headers = Lists.newArrayList();

        for (; start.getTime().before(dateEnd); start.add(Calendar.DAY_OF_YEAR, 1)) {
            headers.add(DateformatUtil.format0(start.getTime()));
        }

        headers.add(DateformatUtil.format0(start.getTime()));

        List<CourseLiveScheduleVo> courseLiveScheduleVos = courseLiveService.mySchedule(examType, subjectId, dateBegin,
                dateEnd, courseName, liveRoomId, user.getId());

        List<List<CourseLiveScheduleVo>> datas = Lists.newArrayList();

        IntStream.range(0, headers.size()).forEach(i ->
            datas.add(null)
        );

        // 表头放入Map
        Map<String, Integer> dateDic = Maps.newHashMap();

        for (int i = 0; i < headers.size(); i++) {
            dateDic.put(headers.get(i), i);
        }

        // 将数据放入对应日期
        courseLiveScheduleVos.forEach(courseLiveScheduleVo -> {
            Integer index = dateDic.get(courseLiveScheduleVo.getDate());

            List<CourseLiveScheduleVo> items = datas.get(index);

            if (items == null) {
                items = Lists.newArrayList();
                datas.set(index, items);
            }

            items.add(courseLiveScheduleVo);
        });

        return ImmutableMap.of("datas", datas);
    }

    /**
     * 修改面试授课类型 添加课程状态判断 直播安排状态抛出异常
     *
     * @param updateCourseLiveCategoryDto 参数
     * @return 结果
     */
    @PostMapping("updateCourseLiveCategory")
    public Boolean updateCourseLiveCategory(@Valid @RequestBody UpdateCourseLiveCategoryDto updateCourseLiveCategoryDto,
                                                    BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        return 0 != courseLiveService.updateCourseLiveCategoryByStatus(updateCourseLiveCategoryDto.getLiveId(), updateCourseLiveCategoryDto.getCourseLiveCategory());
    }

    /**
     * 修改面试授课类型 添加课程状态判断 直播安排状态抛出异常
     *
     * @param updateCourseLiveCategoryDto 参数
     * @return 结果
     */
    @PostMapping("updateCourseLiveCategoryByStatus")
    public Boolean updateCourseLiveCategoryByStatus(@Valid @RequestBody UpdateCourseLiveCategoryDto updateCourseLiveCategoryDto,
                                                    BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        courseLiveService.findCourseStatusByCourseLiveId(updateCourseLiveCategoryDto.getLiveId());//如果为直播安排状态会抛出异常
        return 0 != courseLiveService.updateCourseLiveCategoryByStatus(updateCourseLiveCategoryDto.getLiveId(), updateCourseLiveCategoryDto.getCourseLiveCategory());
    }



    //重排界面获取信息
    @GetMapping("getLiveInfo")
    public List<LiveInfoVo> getLiveInfo(Long liveId, Long liveTeacherId ) {
        if (liveTeacherId == null) {
            throw new BadRequestException("liveTeacherId不能为空");
        }
        CourseLiveTeacher liveTeacher = courseLiveTeacherService.findOne(liveTeacherId);
        CourseLive live = liveTeacher.getCourseLive();
        Course course = live.getCourse();
        LiveInfoVo vo = new LiveInfoVo();

        vo.setExamType(course.getExamType());
        vo.setDate(live.getDate().toString() + " " + TimeRangeUtil.intToDateString(live.getTimeBegin()) + "-" + TimeRangeUtil.intToDateString(live.getTimeEnd()));
        vo.setCourseId(course.getId());
        vo.setCourseName(course.getName());
        vo.setLiveName(live.getName());
        vo.setLiveId(live.getId());
        vo.setId(liveTeacher.getId());
        vo.setSubjectId(liveTeacher.getSubjectId());
        Subject subject = liveTeacher.getSubject();
        if (subject != null) {
            vo.setSubjectName(liveTeacher.getSubject().getName());
        }
        vo.setLevel(liveTeacher.getTeacherCourseLevel());
        Map map=new HashMap();
        map.put("teacherId",liveTeacher.getTeacherId());
        Teacher teacher = liveTeacher.getTeacher();
        if (null != teacher) {
            map.put("teacherName",teacher.getName());
        }
        vo.setTeacher(map);
        vo.setConfirm(liveTeacher.getConfirm());
        vo.setTeacherType(liveTeacher.getTeacherType());
        return Lists.newArrayList(vo);
    }

    /**
     * 提交待沟通教师安排
     *
     * @param submitDGTDto  参数
     * @param bindingResult 验证
     * @return 结果
     */
    @PostMapping("submitCourseLiveTeacherDGT")
    public Boolean submitCourseLiveTeacherDGT(@Valid @RequestBody SubmitDGTDto submitDGTDto, BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        courseLiveService.submitCourseLiveTeacherDGT(submitDGTDto.getLiveId(), submitDGTDto.getLiveTeacherId(),
                submitDGTDto.getTeacherId(), submitDGTDto.getLevel());

        return true;
    }

    /**
     * 修改直播日期时间
     */
    @PostMapping("updateDateTime")
    public Boolean updateDateTime(@Valid @RequestBody UpdateDateTimeDto dto, BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        return courseLiveService.updateDateTime(dto);
    }

    /**
     * 拖动直播日期时间
     */
    @PostMapping("updateDateTimeBatch")
    public Boolean updateDateTimeBatch(@Valid @RequestBody UpdateDateTimeBatchDto dto, BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        courseLiveService.updateDateTimeBatch(dto);
        return true;
    }

    /**
     * 导入课程安排
     */
    @PostMapping("importExcel")
    public Boolean importExcel(@RequestParam("file") MultipartFile file ,Long courseId) throws IOException{
        boolean isExcel2003 = false;
        if (ExcelUtil.isExcel2003(file.getOriginalFilename())) {
            isExcel2003 = true;
        }
        ImportExcelUtil poi = new ImportExcelUtil();
        List<List<List<String>>> list = poi.read(file.getInputStream(), isExcel2003);
        courseLiveService.importExcel(list,courseId);
        return true;
    }

    /**
     * 导入课程安排（教师）
     */
    @PostMapping("importCourse")
    public Boolean importCourse(@RequestParam("file") MultipartFile file ,Long courseId) throws IOException{
        boolean isExcel2003 = false;
        if (ExcelUtil.isExcel2003(file.getOriginalFilename())) {
            isExcel2003 = true;
        }
        ImportExcelUtil poi = new ImportExcelUtil();
        List<List<List<String>>> list = poi.read(file.getInputStream(), isExcel2003);
        courseLiveService.importCourse(list,courseId);

        return true;
    }
}
