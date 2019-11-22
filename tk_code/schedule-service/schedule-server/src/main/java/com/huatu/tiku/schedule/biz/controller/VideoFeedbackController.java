package com.huatu.tiku.schedule.biz.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.schedule.base.exception.BadRequestException;
import com.huatu.tiku.schedule.biz.constant.SessionKey;
import com.huatu.tiku.schedule.biz.domain.*;
import com.huatu.tiku.schedule.biz.dto.*;
import com.huatu.tiku.schedule.biz.enums.FeedbackStatus;
import com.huatu.tiku.schedule.biz.enums.TeacherType;
import com.huatu.tiku.schedule.biz.repository.VideoFeedbackRepository;
import com.huatu.tiku.schedule.biz.service.CourseService;
import com.huatu.tiku.schedule.biz.service.VideoFeedbackInfoService;
import com.huatu.tiku.schedule.biz.util.DateformatUtil;
import com.huatu.tiku.schedule.biz.util.ExcelUtil;
import com.huatu.tiku.schedule.biz.util.ImportExcelUtil;
import com.huatu.tiku.schedule.biz.vo.VideoFeedbackInfoVo;
import com.huatu.tiku.schedule.biz.vo.VideoFeedbackVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**录播课时反馈
 * @author wangjian
 **/
@RequestMapping("videoFeedback")
@RestController
public class VideoFeedbackController {

    private CourseService courseService;

    private VideoFeedbackInfoService videoFeedbackInfoService;

    private VideoFeedbackRepository videoFeedbackRepository;

    @Autowired
    public VideoFeedbackController(CourseService courseService, VideoFeedbackInfoService videoFeedbackInfoService, VideoFeedbackRepository videoFeedbackRepository) {
        this.courseService = courseService;
        this.videoFeedbackInfoService = videoFeedbackInfoService;
        this.videoFeedbackRepository = videoFeedbackRepository;
    }

    /**
     * 点击课程进入 初始数据渲染
     */
    @GetMapping("getCourseInfo")
    public List<Map> getCourseInfo(Long courseId,Integer year,Integer month){
        if(null==year||null==month){
            throw new BadRequestException("请填写年月");
        }
        List<Map> list=Lists.newArrayList();
        Course course = courseService.findOne(courseId);
        Map<Long,String> kv=Maps.newHashMap();
        for (CourseLive courseLive : course.getCourseLives()) {
            Date date = courseLive.getDate();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int calendarYear = calendar.get(Calendar.YEAR);
            int calendarMonth = calendar.get(Calendar.MONTH)+1;
            if(year==calendarYear&&month==calendarMonth){
                for (CourseLiveTeacher courseLiveTeacher : courseLive.getCourseLiveTeachers()) {
                    Long teacherId = courseLiveTeacher.getTeacherId();
                    if(null!=teacherId&& TeacherType.JS.equals(courseLiveTeacher.getTeacherType())){
                        String name = courseLiveTeacher.getTeacher().getName();
                        kv.put(teacherId,name);
                    }
                }
            }
        }
        for (Long id : kv.keySet()) {
            Map<String,Object> map= Maps.newHashMap();
            map.put("id",id);
            map.put("name",kv.get(id));
            list.add(map);
        }
        return list;
    }

    /**
     * 提交反馈
     */
    @PostMapping("submit")
    public Boolean submit(@Valid @RequestBody CreatVideoFeedbackDto dto, BindingResult bindingResult){
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        Long courseId = dto.getCourseId();
        FeedbackStatus[] statusList={FeedbackStatus.YSH,FeedbackStatus.DSH};
        VideoFeedback videoFeedback = videoFeedbackRepository.findByCourseIdAndYearAndMonthAndFeedbackStatusIn(courseId,dto.getYear(),dto.getMonth(),
                Arrays.asList(statusList));
        if(null!=videoFeedback){
            throw new BadRequestException("该课程本月份已反馈");
        }
        videoFeedback=new VideoFeedback();
        videoFeedback.setCourseId(courseId);
        videoFeedback.setFeedbackStatus(FeedbackStatus.DSH);//待审核
        videoFeedback.setYear(dto.getYear());
        videoFeedback.setMonth(dto.getMonth());
        videoFeedback.setDate(DateformatUtil.getLastDate(dto.getYear(),dto.getMonth()));
        videoFeedback = videoFeedbackRepository.save(videoFeedback);
        List<VideoFeedbackInfo> list=Lists.newArrayList();
        List<CreatVideoFeedbackDto.Info> infos = dto.getInfos();
        for (CreatVideoFeedbackDto.Info info : infos) {
            VideoFeedbackInfo videoFeedbackInfo=new VideoFeedbackInfo();
            BeanUtils.copyProperties(info, videoFeedbackInfo);
            if(null==videoFeedbackInfo.getResult()){  //时长未填写
                videoFeedbackInfo.setResult(0d);//设置0
            }
            videoFeedbackInfo.setCourseId(courseId);
            videoFeedbackInfo.setVideoFeedbackId(videoFeedback.getId());
            list.add(videoFeedbackInfo);
        }
        videoFeedbackInfoService.save(list);
        return true;
    }

    /**
     * 反馈记录
     */
    @GetMapping("getSubmitInfo")
    public List<VideoFeedbackInfoVo> getSubmitInfo(Long id){
        List<VideoFeedbackInfo> infos = videoFeedbackInfoService.findByVideoFeedbackId(id);
        List<VideoFeedbackInfoVo> lists= Lists.newArrayList();
        for (VideoFeedbackInfo info : infos) {
            VideoFeedbackInfoVo vo=new VideoFeedbackInfoVo();
            BeanUtils.copyProperties(info, vo);
            vo.setTeacherName(info.getTeacher().getName());
            lists.add(vo);
        }
        return lists;
    }

    /**
     * 反馈列表
     */
    @GetMapping("getList")
    public Page<VideoFeedbackVo> getList(Long courseId,String name,FeedbackStatus status,Pageable page){
        Page<VideoFeedback> result = videoFeedbackInfoService.findVideoFeedbackList(courseId,name,status,page);
        return new PageImpl(result.getContent().stream().map(VideoFeedbackVo::new).collect(Collectors.toList()), page,result.getTotalElements());
    }

    @PostMapping("updateStatus")
    public Object updateStatus(@Valid @RequestBody UpdateFeedBacksDto dto, BindingResult bindingResult){
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        Integer count;
        if(dto.getStatus()){
            count=videoFeedbackRepository.updateStatus(dto.getIds(),FeedbackStatus.YSH);
        }else{
            count=videoFeedbackRepository.updateStatus(dto.getIds(),FeedbackStatus.WTG);
        }
        return count!=0;
    }

    @PostMapping("importExcel")
    public List<Map> importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        boolean isExcel2003 = false;
        if (ExcelUtil.isExcel2003(file.getOriginalFilename())) {
            isExcel2003 = true;
        }
        ImportExcelUtil poi = new ImportExcelUtil();
        List<List<List<String>>> list = poi.read(file.getInputStream(), isExcel2003);
        return videoFeedbackInfoService.importExcel(list);
    }

    @PostMapping("cancel")
    public Boolean cancel(@Valid @RequestBody IdDto dto, BindingResult bindingResult){
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        videoFeedbackRepository.delete(dto.getId());
        return true;
    }

    /**
     * 修改录播信息
     *
     * @param videoFeedbackUpdateDto 修改信息
     * @param authFlag               是否认证
     */
    @PutMapping("updateInfo")
    public void updateInfo(@RequestBody @Valid VideoFeedbackUpdateDto videoFeedbackUpdateDto,
                                @SessionAttribute(SessionKey.FEEDBACK_MODIFY_AUTH) Boolean authFlag) throws Exception {
        videoFeedbackInfoService.updateFeedback(videoFeedbackUpdateDto);
    }
}
