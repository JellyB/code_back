package com.huatu.tiku.schedule.biz.controller;

import com.huatu.tiku.schedule.base.exception.BadRequestException;
import com.huatu.tiku.schedule.biz.domain.CourseLive;
import com.huatu.tiku.schedule.biz.dto.CourseVideoCancelDto;
import com.huatu.tiku.schedule.biz.dto.CreateCourseVideoDto;
import com.huatu.tiku.schedule.biz.dto.UpdateCourseVideoDto;
import com.huatu.tiku.schedule.biz.service.*;
import com.huatu.tiku.schedule.biz.util.DateformatUtil;
import com.huatu.tiku.schedule.biz.vo.CourseVideoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**二期录播课相关接口
 * @author wangjian
 **/
@Slf4j
@RestController
@RequestMapping("courseVideo")
public class CourseVideoController {

    private final CourseLiveService courseLiveService;

    @Autowired
    public CourseVideoController(  CourseLiveService courseLiveService) {
        this.courseLiveService = courseLiveService;
    }

    /**
     * 指定日期指定录影棚 课程详情
     */
    @GetMapping("getVideoRoomInfoByDate")
    public List<CourseVideoVo> getVideoRoomInfoByDate(Long videoRoomId,
                                  @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateBegin,
                                  @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateEnd){
        List<CourseLive> infos=courseLiveService.getVideoRomInfo(videoRoomId,dateBegin,dateEnd);
        return infos.stream().map(CourseVideoVo::new).collect(Collectors.toList());
    }

    /**
     * 添加预约排课 前端控制日期
     */
    @PostMapping("createCourseVideo")
    public CourseVideoVo createCourseVideo(@Valid @RequestBody CreateCourseVideoDto dto,
                                    BindingResult bindingResult){
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        String dateString = DateformatUtil.format0(new Date());
        Date date = DateformatUtil.parse0(dateString);
        if(date.after(dto.getDate())){
            throw new BadRequestException("不能选择历史时间预约，请重新选择");
        }
        if(courseLiveService.timeCheck(dto.getTimeBegin(),dto.getTimeEnd(),dto.getRoomId(),dto.getDate(),null)){
            throw new BadRequestException("本录影棚该时间段,已有录播安排,请重新选择!");  //判断时间是否冲突

        }
        CourseLive courseVideo = courseLiveService.createCourseVideo(dto);//创建的直播
        try {
            courseLiveService.sendCourseVideoConfirmSms(courseVideo);//给教师发送短信
        } catch (Exception e) {
            log.error("发送教师确认短信异常", e);
        }
        return new CourseVideoVo(courseVideo);
    }

    @PostMapping("updateCourseVideo")
    public CourseVideoVo updateCourseVideo(@Valid @RequestBody UpdateCourseVideoDto dto,
                                    BindingResult bindingResult){
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        String dateString = DateformatUtil.format0(new Date());
        Date date = DateformatUtil.parse0(dateString);
        if(date.after(dto.getDate())){
            throw new BadRequestException("不能选择历史时间预约，请重新选择");
        }
        if(courseLiveService.timeCheck(dto.getTimeBegin(),dto.getTimeEnd(),dto.getRoomId(),dto.getDate(),dto.getId())){
            throw new BadRequestException("本录影棚该时间段,已有录播安排,请重新选择!");  //判断时间是否冲突

        }
        CourseLive courseVideo = courseLiveService.updateCourseVideo(dto);//创建的直播
        try {
            courseLiveService.sendCourseVideoConfirmSms(courseVideo);//给教师发送短信
        } catch (Exception e) {
            log.error("发送教师确认短信异常", e);
        }
        return new CourseVideoVo(courseVideo);
    }

    @PostMapping("courseVideoCancel")
    public Boolean courseVideoCancel(@Valid @RequestBody CourseVideoCancelDto courseVideoCancelDto,
                                    BindingResult bindingResult){
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException("课程直播" + bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        courseLiveService.courseVideoCancel(courseVideoCancelDto.getId(),courseVideoCancelDto.getReason());
        return  true;
    }
}
