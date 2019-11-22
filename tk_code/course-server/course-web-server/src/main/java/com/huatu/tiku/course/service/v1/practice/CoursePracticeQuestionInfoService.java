package com.huatu.tiku.course.service.v1.practice;

import java.util.List;

import com.huatu.tiku.entity.CoursePracticeQuestionInfo;

import service.BaseServiceHelper;

/**
 * Created by lijun on 2019/2/21
 */
public interface CoursePracticeQuestionInfoService extends BaseServiceHelper<CoursePracticeQuestionInfo> {

    /**
     * 根据 roomId questionId 列表查询
     */
    List<CoursePracticeQuestionInfo> listByRoomIdAndQuestionId(Long roomId, List<Long> questionIdList);

    /**
	 * 获取房间已作答试题集合
	 * @param roomId
	 * @return
	 */
	List<Integer> getQuestionsInfoByRoomId(Long roomId);

    /**
     * 生成答题卡信息
     * @param questionIds 试题IDs
     * @param courseUserStrs 学员课程keys
     */
    void generateAnswerCardInfo(List<Integer> questionIds, List <String> courseUserStrs, Long roomId);
}
