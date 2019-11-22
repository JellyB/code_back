package com.huatu.tiku.schedule.biz.controller;

import java.util.List;

import javax.validation.Valid;

import com.huatu.tiku.schedule.biz.dto.*;
import com.huatu.tiku.schedule.biz.enums.CourseConfirmStatus;
import com.huatu.tiku.schedule.biz.enums.CoursePhase;
import com.huatu.tiku.schedule.biz.enums.TeacherCourseLevel;
import com.huatu.tiku.schedule.biz.service.CourseLiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.huatu.tiku.schedule.base.exception.BadRequestException;
import com.huatu.tiku.schedule.biz.domain.CourseLiveTeacher;
import com.huatu.tiku.schedule.biz.service.CourseLiveTeacherService;

/**
 * 课程直播教师Controller
 *
 * @author Geek-S
 */
@RestController
@RequestMapping("courseLiveTeacher")
public class CourseLiveTeacherController {

    private final CourseLiveTeacherService courseLiveTeacherService;
    private final CourseLiveService courseLiveService;

    @Autowired
    public CourseLiveTeacherController(CourseLiveTeacherService courseLiveTeacherService,
                                       CourseLiveService courseLiveService) {
        this.courseLiveTeacherService = courseLiveTeacherService;
        this.courseLiveService=courseLiveService;
    }

    /**
     * 更新绑定教师 添加课程状态判断 直播安排状态抛出异常
     * @param bindTeacherDto 课程直播
     * @return 课程直播
     */
    @PostMapping("bindTeacher")
    public Boolean bindTeacher(@Valid @RequestBody BindTeacherDto bindTeacherDto, BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        courseLiveTeacherService.findCourseStatusByCourseLiveTeacherId(bindTeacherDto.getCourseLiveTeacherId());//如果为直播安排状态会抛出异常

        int flag = courseLiveTeacherService.bindTeacher(bindTeacherDto.getCourseLiveTeacherId(),
                bindTeacherDto.getTeacherId());

        if (flag == 0) {
            throw new BadRequestException("课程直播教师ID错误");
        }

        return true;
    }

    /**
     * 创建课程直播教师
     *
     * @param courseLiveTeacherDto 课程直播
     * @return 课程直播教师
     */
    @PostMapping("createCourseLiveTeacher")
    public CourseLiveTeacher createCourseLiveTeacher(
            @Valid @RequestBody CreateCourseLiveTeacherDto courseLiveTeacherDto, BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        Long subjectId=courseLiveService.getCourseSubjectId(courseLiveTeacherDto.getCourseLiveId());//课程指定科目
        CourseLiveTeacher courseLiveTeacher = new CourseLiveTeacher();
        courseLiveTeacher.setCourseLiveId(courseLiveTeacherDto.getCourseLiveId());
        courseLiveTeacher.setConfirm(CourseConfirmStatus.DQR);//设置默认确认状态为待确认
        courseLiveTeacher.setCoursePhase(CoursePhase.ONE);//默认一阶段
        courseLiveTeacher.setSubjectId(subjectId);
        courseLiveTeacher.setTeacherCourseLevel(TeacherCourseLevel.GOOD);//默认专长级别
        courseLiveTeacherService.save(courseLiveTeacher);

        return courseLiveTeacher;
    }

    /**
     * 创建课程直播教师 添加课程状态判断 直播安排状态抛出异常
     * @param courseLiveTeacherDto 课程直播
     * @return 课程直播教师
     */
    @PostMapping("createCourseLiveTeacherByStatus")
    public CourseLiveTeacher createCourseLiveTeacherByStatus(
            @Valid @RequestBody CreateCourseLiveTeacherDto courseLiveTeacherDto, BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        courseLiveService.findCourseStatusByCourseLiveId(courseLiveTeacherDto.getCourseLiveId());//如果为直播安排状态会抛出异常
        Long subjectId=courseLiveService.getCourseSubjectId(courseLiveTeacherDto.getCourseLiveId());
        CourseLiveTeacher courseLiveTeacher = new CourseLiveTeacher();
        courseLiveTeacher.setCourseLiveId(courseLiveTeacherDto.getCourseLiveId());
        courseLiveTeacher.setConfirm(CourseConfirmStatus.DQR);//设置默认确认状态为待确认
        courseLiveTeacher.setSubjectId(subjectId);
        courseLiveTeacher.setCoursePhase(CoursePhase.ONE);//默认一阶段
        courseLiveTeacher.setTeacherCourseLevel(TeacherCourseLevel.GOOD);//默认授课级别
        courseLiveTeacherService.save(courseLiveTeacher);

        return courseLiveTeacher;
    }

    /**
     * 删除课程直播教师
     *
     * @param idDto 课程直播ID
     * @return 操作结果
     */
    @PostMapping("deleteCourseLiveTeacher")
    public Boolean deleteCourseLiveTeacher(@Valid @RequestBody IdDto idDto, BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException("课程直播教师" + bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        courseLiveTeacherService.delete(idDto.getId());

        return true;
    }
    /**
     * 删除课程直播教师 添加课程状态判断 直播安排状态抛出异常
     *
     * @param idDto 课程直播ID
     * @return 操作结果
     */
    @PostMapping("deleteCourseLiveTeacherByStatus")
    public Boolean deleteCourseLiveTeacherByStatus(@Valid @RequestBody IdDto idDto, BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException("课程直播教师" + bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        courseLiveTeacherService.findCourseStatusByCourseLiveTeacherId(idDto.getId());//如果为直播安排状态会抛出异常

        courseLiveTeacherService.delete(idDto.getId());

        return true;
    }



    /**
     * 批量添加直播安排
     * 运营批量添加课程直播教师
     * @param courseLiveTeacherBatchDto 课程直播教师
     * @return 课程直播教师
     */
    @PostMapping("saveCourseLiveTeacherBatch")
    public List<CourseLiveTeacher> saveCourseLiveTeacherBatch(
            @Valid @RequestBody SaveCourseLiveTeacherBatchDto courseLiveTeacherBatchDto, BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return courseLiveTeacherService.saveCourseLiveTeacherBatch(courseLiveTeacherBatchDto,false);
    }

    /**
     * 批量覆盖直播安排
     * 运营批量添加课程直播教师
     * @param courseLiveTeacherBatchDto 课程直播教师
     * @return 课程直播教师
     */
    @PostMapping("coverCourseLiveTeacherBatch")
    public List<CourseLiveTeacher> coverCourseLiveTeacherBatch(
            @Valid @RequestBody SaveCourseLiveTeacherBatchDto courseLiveTeacherBatchDto, BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return courseLiveTeacherService.saveCourseLiveTeacherBatch(courseLiveTeacherBatchDto,true);
    }

    /**
     * 修改阶段
     * @param saveCoursePhaseDto 参数
     * @return 结果
     */
    @PostMapping("updateCoursePhase")
    public Boolean updateCoursePhase(@Valid @RequestBody UpdateCoursePhaseDto saveCoursePhaseDto,
                                     BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        return 0!=courseLiveTeacherService.savaCoursePhase(saveCoursePhaseDto.getCourseLiveTeacherId(),saveCoursePhaseDto.getCoursePhase());
    }
    /**
     * 修改阶段 添加课程状态判断 直播安排状态抛出异常
     * @param saveCoursePhaseDto 参数
     * @return 结果
     */
    @PostMapping("updateCoursePhaseByStatus")
    public Boolean updateCoursePhaseByStatus(@Valid @RequestBody UpdateCoursePhaseDto saveCoursePhaseDto,
                                     BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        courseLiveTeacherService.findCourseStatusByCourseLiveTeacherId(saveCoursePhaseDto.getCourseLiveTeacherId());//如果为直播安排状态会抛出异常
        return 0!=courseLiveTeacherService.savaCoursePhase(saveCoursePhaseDto.getCourseLiveTeacherId(),saveCoursePhaseDto.getCoursePhase());
    }

    /**
     * 修改模块
     * @param updateModuleDto 参数
     * @return 结果
     */
    @PostMapping("updateModule")
    public Boolean updateModule(@Valid @RequestBody UpdateModuleDto updateModuleDto,
                                BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        return 0!=courseLiveTeacherService.savaModule(updateModuleDto.getCourseLiveTeacherId(),updateModuleDto.getModuleId());
    }

    /**
     * 修改模块 添加课程状态判断 直播安排状态抛出异常
     * @param updateModuleDto 参数
     * @return 结果
     */
    @PostMapping("updateModuleByStatus")
    public Boolean updateModuleByStatus(@Valid @RequestBody UpdateModuleDto updateModuleDto,
                                BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        courseLiveTeacherService.findCourseStatusByCourseLiveTeacherId(updateModuleDto.getCourseLiveTeacherId());//如果为直播安排状态会抛出异常
        return 0!=courseLiveTeacherService.savaModule(updateModuleDto.getCourseLiveTeacherId(),updateModuleDto.getModuleId());
    }

    /**
     * 修改授课科目
     * @param updateSubjectDto 参数
     * @return 结果
     */
    @PostMapping("updateSubject")
    public Boolean updateSubject(@Valid @RequestBody UpdateSubjectDto updateSubjectDto,
                                BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        return 0!=courseLiveTeacherService.savaSubject(updateSubjectDto.getCourseLiveTeacherId(),updateSubjectDto.getSubjectId());
    }

    /**
     * 修改授课科目 添加课程状态判断 直播安排状态抛出异常
     * @param updateSubjectDto 参数
     * @return 结果
     */
    @PostMapping("updateSubjectByStatus")
    public Boolean updateSubjectByStatus(@Valid @RequestBody UpdateSubjectDto updateSubjectDto,
                                BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        courseLiveTeacherService.findCourseStatusByCourseLiveTeacherId(updateSubjectDto.getCourseLiveTeacherId());//如果为直播安排状态会抛出异常
        return 0!=courseLiveTeacherService.savaSubject(updateSubjectDto.getCourseLiveTeacherId(),updateSubjectDto.getSubjectId());
    }

    /**
     * 修改授课级别
     * @param savaLiveTeacherLevelDto 参数
     * @return 结果
     */
    @PostMapping("updateLiveTeacherLevel")
    public Boolean updateLiveTeacherLevel(@Valid @RequestBody UpdateLiveTeacherLevelDto savaLiveTeacherLevelDto,
                                          BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        return 0!=courseLiveTeacherService.savaLiveTeacherLevel(savaLiveTeacherLevelDto.getCourseLiveTeacherId(),savaLiveTeacherLevelDto.getTeacherCourseLevel());
    }

    /**
     * 修改授课级别 添加课程状态判断 直播安排状态抛出异常
     * @param savaLiveTeacherLevelDto 参数
     * @return 结果
     */
    @PostMapping("updateLiveTeacherLevelByStatus")
    public Boolean updateLiveTeacherLevelByStatus(@Valid @RequestBody UpdateLiveTeacherLevelDto savaLiveTeacherLevelDto,
                                          BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        courseLiveTeacherService.findCourseStatusByCourseLiveTeacherId(savaLiveTeacherLevelDto.getCourseLiveTeacherId());//如果为直播安排状态会抛出异常
        return 0!=courseLiveTeacherService.savaLiveTeacherLevel(savaLiveTeacherLevelDto.getCourseLiveTeacherId(),savaLiveTeacherLevelDto.getTeacherCourseLevel());
    }

}
