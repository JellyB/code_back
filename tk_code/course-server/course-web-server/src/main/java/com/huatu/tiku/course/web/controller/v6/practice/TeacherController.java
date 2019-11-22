package com.huatu.tiku.course.web.controller.v6.practice;

import com.huatu.common.SuccessMessage;
import com.huatu.springboot.web.version.mapping.annotation.ApiVersion;
import com.huatu.tiku.course.service.v1.practice.CoursewarePracticeQuestionInfoService;
import com.huatu.tiku.course.service.v1.practice.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

/**
 * Created by lijun on 2019/2/18
 */
@RestController
@RequestMapping("practice/teacher")
@ApiVersion("v6")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;
    
    private final CoursewarePracticeQuestionInfoService coursewarePracticeQuestionInfoService;

    /**
     * 根据 roomId 获取试题详情
     */
    @GetMapping("{roomId}/getQuestionInfo")
    public Object getQuestionInfoByRoomId(@PathVariable Long roomId) throws ExecutionException, InterruptedException {
        return teacherService.getQuestionInfoByRoomId(roomId);
    }

    /**
     * 点击练一下
     */
    @PutMapping("{roomId}/{questionId}/practice")
    public Object putQuestionPractice(@PathVariable Long roomId, @PathVariable Long questionId, @RequestParam Integer practiceTime) {
        teacherService.saveQuestionPracticeInfo(roomId, questionId, practiceTime);
        return SuccessMessage.create();
    }

    /**
     * 更新各个试题绑定开始考试时间
     */
    @PutMapping("{roomId}/{questionId}/updateQuestionPracticeTime")
    public Object updateQuestionPracticeTime(@PathVariable Long roomId, @PathVariable Long questionId, @RequestParam Integer practiceTime) {
        teacherService.updateQuestionPracticeTime(roomId, questionId, practiceTime);
        return SuccessMessage.create();
    }

    /**
     * 获取答题情况
     */
    @GetMapping("{roomId}/{questionId}/questionStatistics")
    public Object getQuestionStatistics(@PathVariable Long roomId, @PathVariable Long questionId) throws ExecutionException, InterruptedException {
        return teacherService.getQuestionStatisticsByRoomIdAndQuestionId(roomId, questionId);
    }

    /**
     * 获取试题排名信息
     */
    @GetMapping("{roomId}/questionRankInfo")
    public Object getQuestionRankInfo(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        page = page < 1 ? 1 : page;
        if (pageSize < 0 || pageSize > 50) {
            pageSize = 10;
        }
        return teacherService.getQuestionRankInfo(roomId, page, pageSize);
    }
    
    /**
     * 老师结束作答默认将开始答题时间置为-1
     * @param roomId
     * @param questionId
     * @return
     */
	@PostMapping("{roomId}/{questionId}/stopAnswer")
	public Object stopAnswer(@PathVariable Long roomId, @PathVariable Long questionId) {
		teacherService.stopAnswer(roomId, questionId);
		return SuccessMessage.create();
	}

    /**
     * 获取课件下试题作答信息
     */
    @GetMapping("{roomId}/couserwareAnswerInfo")
    public Object getCourseQuestionAnswerInfo(
            @PathVariable Long roomId) {
        return teacherService.getCoursewareAnswerQuestionInfo(roomId);
    }

    /**
     * 查询课件的随堂练习正确率
     */
    @GetMapping("{roomId}/{coursewareId}/couserwareAccuracy")
    public Object getCoursewareQuestionAccuracy(
            @PathVariable Long roomId,
            @PathVariable Long coursewareId) {
        return teacherService.getCourseRightRate(coursewareId, roomId);
    }
    
    /**
     * 蓝色后台获取直播随堂练统计数据
     * @param roomId
     * @param coursewareId
     * @return
     */
    @GetMapping("{roomId}/{coursewareId}/getCoursePracticeData")
    public Object getCoursePracticeData(
            @PathVariable Long roomId,
            @PathVariable Long coursewareId) {
        return coursewarePracticeQuestionInfoService.findByCoursewareIdAndRoomId(roomId, coursewareId);
    }
}
