package com.huatu.tiku.course.service.v1.practice;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.github.pagehelper.PageInfo;
import com.huatu.tiku.course.bean.practice.PracticeRoomRankUserBo;
import com.huatu.tiku.course.bean.practice.QuestionMetaBo;

/**
 * Created by lijun on 2019/2/21
 */
public interface TeacherService {

    /**
     * 通过roomId 获取绑定的试题信息
     */
	Map<String,Object> getQuestionInfoByRoomId(Long roomId) throws ExecutionException, InterruptedException;

    /**
     * 存储 试题练习信息
     */
    void saveQuestionPracticeInfo(Long roomId, Long questionId, Integer practiceTime);

    /**
     * 更新预留考试时间
     */
    void updateQuestionPracticeTime(Long roomId, Long questionId, Integer practiceTime);

    /**
     * 获取答题情况
     */
    QuestionMetaBo getQuestionStatisticsByRoomIdAndQuestionId(Long roomId, Long questionId) throws ExecutionException, InterruptedException;

    /**
     * 教师端分页获取统计数据
     */
    PageInfo<PracticeRoomRankUserBo> getQuestionRankInfo(Long roomId, Integer page, Integer pageSize);

    /**
     * 结束作答指定题目
     * @param roomId
     * @param questionId
     */
	void stopAnswer(Long roomId, Long questionId);

    /**
     * 获取课件下试题的作答信息
     * @param roomId
     * @return
     */
    List<QuestionMetaBo> getCoursewareAnswerQuestionInfo(Long roomId);

    /**
     * 根据课件Id查询课件的随堂练习正确率
     * @param coursewareId 课件Id
     * @return
     */
    Integer getCourseRightRate(Long coursewareId,Long roomId);
}
