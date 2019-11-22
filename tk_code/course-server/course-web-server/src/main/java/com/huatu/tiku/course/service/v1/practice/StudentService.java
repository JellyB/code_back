package com.huatu.tiku.course.service.v1.practice;

import com.huatu.tiku.course.bean.practice.PracticeRoomRankUserBo;
import com.huatu.tiku.course.bean.practice.StudentQuestionMetaBo;

import java.util.List;
import java.util.Map;

/**
 * Created by lijun on 2019/2/27
 */
public interface StudentService {

    /**
     * 用户保存答案
     *
     * @param roomId     房间ID
     * @param courseId   课程ID
     * @param userId     用户ID
     * @param userName   用户名称
     * @param questionId 试题ID
     * @param answer     答案
     * @param time       用时
     */
	Map<String,Object> putAnswer(Long roomId, Long courseId, Integer userId, String userName, Long questionId, String answer, Integer time);

    /**
     * 获取 答题情况
     *
     * @param userId     用户ID
     * @param roomId     房间ID
     * @param courseId   课件ID
     * @param questionId 试题ID
     */
    StudentQuestionMetaBo getStudentQuestionMetaBo(Integer userId, Long roomId, Long courseId, Long questionId);

    /**
     * 获取排名信息
     *
     * @param roomId 房间ID
     * @param start  开始下标
     * @param end    结束下标
     */
    List<PracticeRoomRankUserBo> listPracticeRoomRankUser(Long roomId, Integer start, Integer end);

    /**
     * 获取用户在当前房间中的统计信息
     *
     * @param userId   用户ID
     * @param courseId 房间ID
     */
    PracticeRoomRankUserBo getUserRankInfo(Integer userId, Long courseId);

}
