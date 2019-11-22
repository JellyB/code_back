package com.huatu.tiku.schedule.biz.service.imple;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.schedule.base.exception.BadRequestException;
import com.huatu.tiku.schedule.base.service.impl.BaseServiceImpl;
import com.huatu.tiku.schedule.biz.domain.*;
import com.huatu.tiku.schedule.biz.dto.VideoFeedbackUpdateDto;
import com.huatu.tiku.schedule.biz.enums.CourseCategory;
import com.huatu.tiku.schedule.biz.enums.CourseStatus;
import com.huatu.tiku.schedule.biz.enums.ExamType;
import com.huatu.tiku.schedule.biz.enums.FeedbackStatus;
import com.huatu.tiku.schedule.biz.repository.CourseRepository;
import com.huatu.tiku.schedule.biz.repository.TeacherRepository;
import com.huatu.tiku.schedule.biz.repository.VideoFeedbackInfoRepository;
import com.huatu.tiku.schedule.biz.repository.VideoFeedbackRepository;
import com.huatu.tiku.schedule.biz.service.FeedbackUpdateLogService;
import com.huatu.tiku.schedule.biz.service.VideoFeedbackInfoService;
import com.huatu.tiku.schedule.biz.util.DateformatUtil;
import com.huatu.tiku.schedule.biz.util.TimeUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

import javax.persistence.criteria.*;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author wangjian
 **/
@Service
public class VideoFeedbackInfoServiceImpl extends BaseServiceImpl<VideoFeedbackInfo, Long> implements VideoFeedbackInfoService {

    private VideoFeedbackInfoRepository videoFeedbackInfoRepository;

    private VideoFeedbackRepository videoFeedbackRepository;

    private FeedbackUpdateLogService feedbackUpdateLogService;

    public VideoFeedbackInfoServiceImpl(VideoFeedbackInfoRepository videoFeedbackInfoRepository, VideoFeedbackRepository videoFeedbackRepository, CourseRepository courseRepository, TeacherRepository teacherRepository, FeedbackUpdateLogService feedbackUpdateLogService) {
        this.videoFeedbackInfoRepository = videoFeedbackInfoRepository;
        this.videoFeedbackRepository = videoFeedbackRepository;
        this.courseRepository = courseRepository;
        this.teacherRepository = teacherRepository;
        this.feedbackUpdateLogService = feedbackUpdateLogService;
    }

    @Override
    public List<VideoFeedbackInfo> findByVideoFeedbackId(Long id) {
        return videoFeedbackInfoRepository.findByVideoFeedbackId(id);
    }

    @Override
    public Page<VideoFeedback> findVideoFeedbackList(Long courseId, String name, FeedbackStatus status, Pageable page) {
        Specification<VideoFeedback> querySpecific = new Specification<VideoFeedback>() {
            @Override
            public Predicate toPredicate(Root<VideoFeedback> root, CriteriaQuery<?> criteriaQuery,
                                         CriteriaBuilder criteriaBuilder) {

                List<Predicate> predicates = new ArrayList<>();

                if (status != null) {
                    predicates.add(criteriaBuilder.equal(root.get("feedbackStatus"), status));
                }
                Join<VideoFeedback, Course> course=null;
                if (name != null) {
                    if(course==null){
                        course = root.join("course");
                    }
                    predicates.add(criteriaBuilder.like(course.get("name"), "%" + name + "%"));
                }
                if (courseId != null) {
                    if(course==null){
                        course = root.join("course");
                    }
                    predicates.add(criteriaBuilder.equal(course.get("id"), courseId));
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        List<Sort.Order> list = new ArrayList();
        list.add(new Sort.Order(Sort.Direction.DESC, "id"));//创建日期倒序
        Pageable pageable = new PageRequest(page.getPageNumber(), page.getPageSize(), new Sort(list));
        Page<VideoFeedback> classHourFeedbacks = videoFeedbackRepository.findAll(querySpecific, pageable);
        return classHourFeedbacks;
    }

    private CourseRepository courseRepository;
    private TeacherRepository teacherRepository;

    @Override
    @Transactional
    public List<Map> importExcel(List<List<List<String>>> list) {
        List<Map> resultList= Lists.newArrayList();
        for (List<List<String>> sheet : list) {
            for(int i=1;i<sheet.size();i++){
                Map map= Maps.newHashMap();
                Long courseId=null;
                Long teacherId=null;
                Double doubleResult=null;
                String remark=null;
                Integer year=null;
                Integer month=null;
                List<String> row = sheet.get(i);
                String date = row.get(4);
                if(StringUtils.isBlank(date)){
                    throw new BadRequestException("第"+i+"行反馈月份不能为空");
                }else{
                    try {
                        year  = Integer.valueOf(date.substring(0,4));
                        month = Double.valueOf(date.substring(4)).intValue();
                    } catch (NumberFormatException e) {
                        throw new BadRequestException("第"+i+"行日期错误");
                    }
                }
                String courseName = row.get(3).trim().replace("\n","");
                if(StringUtils.isBlank(courseName)){
                    throw new BadRequestException("第"+i+"行反馈课程不能为空");
                }else {
                    List<Course> courses = courseRepository.findByName(courseName);
                    if (null != courses && !courses.isEmpty()) {
                        if(courses.size()>1){
                            throw new BadRequestException("第"+i + "行反馈课程匹配到多个同名课程");
                        }else{
                            courseId=courses.get(0).getId();//取到课程id
                            map.put("courseName",courses.get(0).getName());
                        }
                    } else {
                        Course course=new Course();
                        course.setSeparatorFlag(false);
                        course.setSatFlag(false);
                        course.setSunFlag(false);
                        course.setControllerFlag(false);
                        course.setCompereFlag(false);
                        course.setAssistantFlag(false);
                        course.setSchoolType(null);
                        course.setStatus(CourseStatus.WC);
                        course.setExamType(ExamType.GWY);
                        course.setName(courseName);
                        course.setCourseCategory(CourseCategory.VIDEO);
                        course=courseRepository.save(course);
                        courseId=course.getId();//取到课程id
                        map.put("courseName",courseName);
//                        throw new BadRequestException("第"+i + "行反馈课程未匹配到对应课程");
                    }
                }
                String teacherName = row.get(0).trim().replace("\n","");
                if(StringUtils.isBlank(teacherName)){
                    throw new BadRequestException("第"+i+"行教师姓名不能为空");
                }else{
                    Teacher teacher = teacherRepository.findOneByName(teacherName);
                    if(teacher==null){
                        throw new BadRequestException("第"+i+"行教师姓名未找到对应教师");
                    }
                    teacherId=teacher.getId();//取得教师id
                    map.put("teacherName",teacher.getName());
                }
                String result = row.get(1);
                if(StringUtils.isBlank(result)){
                    throw new BadRequestException("第"+i+"行剪辑时长不能为空");
                }else{
                    BigDecimal bigDecimal = TimeUtil.minut2Hour(result);
                    doubleResult=bigDecimal.doubleValue();
//                    doubleResult=Double.valueOf(result);
                }
                remark = row.get(2);
                FeedbackStatus[] statusList={FeedbackStatus.YSH,FeedbackStatus.DSH};
                VideoFeedback videoFeedback = videoFeedbackRepository.findByCourseIdAndYearAndMonthAndFeedbackStatusIn(courseId,year,month, Arrays.asList(statusList));
                if(null==videoFeedback){
                    videoFeedback=new VideoFeedback();
                    videoFeedback.setCourseId(courseId);
                    videoFeedback.setFeedbackStatus(FeedbackStatus.DSH);//待审核
                    videoFeedback.setYear(year);
                    videoFeedback.setMonth(month);
                    videoFeedback.setDate(DateformatUtil.getLastDate(year,month));
                    videoFeedback = videoFeedbackRepository.save(videoFeedback);
                }else{
                    if(FeedbackStatus.YSH.equals(videoFeedback.getFeedbackStatus())){
                        throw new BadRequestException("第"+i+"行提交的"+courseName+"-"+date+",此反馈已经审核完成,请勿对该课程进行提交");
                    }
                }
                List<VideoFeedbackInfo> infos = videoFeedback.getInfos();
                if(null!=infos&&!infos.isEmpty()){
                    for (VideoFeedbackInfo info : infos) {
                        if(info.getTeacherId().equals(teacherId)){
                            throw new BadRequestException("第"+i+"行提交的教师指定月份反馈已提交,请勿重复提交");
                        }
                    }
                }
                VideoFeedbackInfo videoFeedbackInfo=new VideoFeedbackInfo();
                videoFeedbackInfo.setCourseId(courseId);
                videoFeedbackInfo.setRemark(remark);
                videoFeedbackInfo.setTeacherId(teacherId);
                videoFeedbackInfo.setResult(doubleResult);
                videoFeedbackInfo.setVideoFeedbackId(videoFeedback.getId());
                videoFeedbackInfoRepository.save(videoFeedbackInfo);
                map.put("courseId",courseId);
                map.put("teacherId",teacherId);
                map.put("result",doubleResult);
                map.put("remark",remark);
                map.put("month",month);
                map.put("year",year);
                resultList.add(map);
            }
        }
        return resultList;
    }

    @Override
    public void updateFeedback(VideoFeedbackUpdateDto feedbackUpdateDto) throws Exception {
        VideoFeedbackInfo videoFeedbackInfo = videoFeedbackInfoRepository.findOne(feedbackUpdateDto.getId());

        String methodSuffix = feedbackUpdateDto.getField().substring(0, 1).toUpperCase() + feedbackUpdateDto.getField().substring(1);

        String getter = "get" + methodSuffix;
        String setter = "set" + methodSuffix;

        Method getterMethod = ReflectionUtils.findMethod(VideoFeedbackInfo.class, getter);

        Class fieldType = ReflectionUtils.findField(VideoFeedbackInfo.class, feedbackUpdateDto.getField()).getType();

        Object oriValue = getterMethod.invoke(videoFeedbackInfo);

        Method setterMethod = ReflectionUtils.findMethod(VideoFeedbackInfo.class, setter, fieldType);

        setterMethod.invoke(videoFeedbackInfo, fieldType == Double.class ? Double.parseDouble(feedbackUpdateDto.getValue()) : feedbackUpdateDto.getValue());

        videoFeedbackInfoRepository.save(videoFeedbackInfo);

        FeedbackUpdateLog feedbackUpdateLog = new FeedbackUpdateLog();
        feedbackUpdateLog.setFeedbackId(videoFeedbackInfo.getVideoFeedbackId());
        feedbackUpdateLog.setClassHourId(feedbackUpdateDto.getId());
        feedbackUpdateLog.setField("result");
        feedbackUpdateLog.setValue(feedbackUpdateDto.getValue());
        feedbackUpdateLog.setOriValue(oriValue.toString());
        feedbackUpdateLog.setType(1);

        feedbackUpdateLogService.save(feedbackUpdateLog);
    }
}
