package com.huatu.tiku.schedule.biz.controller;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.huatu.tiku.schedule.biz.domain.ClassHourFeedback;
import com.huatu.tiku.schedule.biz.domain.ClassHourInfo;
import com.huatu.tiku.schedule.biz.domain.FeedbackUpdateLog;
import com.huatu.tiku.schedule.biz.domain.VideoFeedbackInfo;
import com.huatu.tiku.schedule.biz.service.*;
import com.huatu.tiku.schedule.biz.vo.FeedbackUpdateLogVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("feedbackUpdateLog")
public class FeedbackUpdateLogController {

    @Autowired
    private FeedbackUpdateLogService feedbackUpdateLogService;

    @Autowired
    private ClassHourInfoService classHourInfoService;

    @Autowired
    private ClassHourFeedbackService classHourFeedbackService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private VideoFeedbackInfoService videoFeedbackInfoService;

    private Map<String, String> FIELD_NAME_DIC = new ImmutableMap.Builder<String, String>()
            .put("reallyExam", "真题题数").put("reallyHour", "真题课时")
            .put("simulationExam", "模拟题数").put("simulationHour", "模拟题课时")
            .put("articleHour", "文章课时").put("audioHour", "音频课时")
            .put("result", "剪辑时长").put("remark", "备注").build();

    /**
     * 获取反馈修改记录
     *
     * @param page 分页信息
     * @return 修改记录
     */
    @GetMapping
    public Page<FeedbackUpdateLogVo> list(Integer type, Long feedbackId, Pageable page) {
        page = new PageRequest(page.getPageNumber(), page.getPageSize(), new Sort(Sort.Direction.DESC, "id"));

        Page<FeedbackUpdateLog> logs = feedbackUpdateLogService.findByTypeAndFeedbackId(type, feedbackId, page);

        List<FeedbackUpdateLogVo> feedbackUpdateLogVos = Lists.newArrayList();

        logs.forEach(log -> {
            FeedbackUpdateLogVo feedbackUpdateLogVo = new FeedbackUpdateLogVo();

            if (log.getType() == 0) {
                ClassHourInfo classHourInfo = classHourInfoService.findOne(log.getClassHourId());
                ClassHourFeedback classHourFeedback = classHourFeedbackService.findOne(classHourInfo.getFeedbackId());

                feedbackUpdateLogVo.setYear(classHourFeedback.getYear());
                feedbackUpdateLogVo.setMonth(classHourFeedback.getMonth());
                feedbackUpdateLogVo.setExamType(classHourFeedback.getExamType().getText());
                feedbackUpdateLogVo.setSubject(classHourFeedback.getSubject().getName());
                feedbackUpdateLogVo.setTeacherName(classHourInfo.getTeacher().getName());
            } else {
                VideoFeedbackInfo videoFeedbackInfo = videoFeedbackInfoService.findOne(log.getClassHourId());

                feedbackUpdateLogVo.setTeacherName(videoFeedbackInfo.getTeacher().getName());
            }

            feedbackUpdateLogVo.setId(log.getId());
            feedbackUpdateLogVo.setFeedbackId(log.getFeedbackId());
            feedbackUpdateLogVo.setField(FIELD_NAME_DIC.get(log.getField()));
            feedbackUpdateLogVo.setValue(log.getValue());
            feedbackUpdateLogVo.setOriValue(log.getOriValue());
            feedbackUpdateLogVo.setOperator(teacherService.findOne(log.getCreatedBy()).getName());
            feedbackUpdateLogVo.setOperationTime(log.getCreatedDate());

            feedbackUpdateLogVos.add(feedbackUpdateLogVo);
        });

        return new PageImpl<>(feedbackUpdateLogVos, page, logs.getTotalElements());
    }
}
