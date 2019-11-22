package com.huatu.tiku.course.service.v1.impl.practice;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.huatu.tiku.course.bean.practice.PracticeRoomRankUserBo;
import com.huatu.tiku.course.bean.practice.QuestionInfo;
import com.huatu.tiku.course.bean.practice.StudentQuestionMetaBo;
import com.huatu.tiku.course.service.cache.CoursePracticeCacheKey;
import com.huatu.tiku.course.service.v1.practice.CoursePracticeQuestionInfoService;
import com.huatu.tiku.course.service.v1.practice.QuestionInfoService;
import com.huatu.tiku.course.service.v1.practice.StudentService;

import lombok.RequiredArgsConstructor;

/**
 * Created by lijun on 2019/2/27
 */
@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final QuestionInfoService questionInfoService;
    private final PracticeMetaComponent practiceMetaComponent;

    @Autowired
    private CoursePracticeQuestionInfoService coursePracticeQuestionInfoService;
    private final RedisTemplate redisTemplate;
    @Override
    public Map<String,Object> putAnswer(Long roomId, Long courseId, Integer userId, String userName, Long questionId, String answer, Integer time) {
    	//判断是否已经作答
    	if(!practiceMetaComponent.checkUserHasAnswer(userId, courseId, questionId)) {
    		return new HashMap<>();
    	}
    	List<QuestionInfo> baseQuestionInfoList = questionInfoService.getBaseQuestionInfo(Lists.newArrayList(questionId));
        if (CollectionUtils.isEmpty(baseQuestionInfoList)) {
            return null;
        }
        if (StringUtils.isEmpty(answer)) {
            practiceMetaComponent.buildUserQuestionMeta(userId, courseId, questionId, answer, time, 0);
        }
        final QuestionInfo questionInfo = baseQuestionInfoList.get(0);
        answer = formatAnswer(answer);
        boolean isCorrect = answer.equals(questionInfo.getAnswer());
        practiceMetaComponent.buildMetaInfo(userId, userName, roomId, courseId, questionId, answer, time, isCorrect ? 1 : 2);
        //存储答题用户信息到set中
        practiceMetaComponent.setRoomInfoMeta(userId, roomId, courseId);
        //返回做题结果 TODO 送金币
        return ImmutableMap.of("isCorrect",isCorrect, "coin",2);
    }

    @Override
    public StudentQuestionMetaBo getStudentQuestionMetaBo(Integer userId, Long roomId, Long courseId, Long questionId) {
        StudentQuestionMetaBo studentQuestionMetaBo = practiceMetaComponent.getStudentQuestionMetaBo(userId, roomId, courseId, questionId);
        //构建用户的答题信息
        List<String> roomPracticedQuestionList = practiceMetaComponent.getRoomPracticedQuestion(roomId);
        //设置已答总题量
        studentQuestionMetaBo.setTotalQuestionNum(roomPracticedQuestionList.size());
        return studentQuestionMetaBo;
    }

    @Override
	public List<PracticeRoomRankUserBo> listPracticeRoomRankUser(Long roomId, Integer start, Integer end) {
		return practiceMetaComponent.getRoomRankInfo(roomId, start, end).stream()
				.filter(practiceRoomRankUserBo -> practiceRoomRankUserBo.getTotalScore() > 0)
				.collect(Collectors.toList());

	}

    @Override
    public PracticeRoomRankUserBo getUserRankInfo(Integer userId, Long courseId) {
        return practiceMetaComponent.getPracticeRoomRankUser(userId, courseId);
    }

    /**
     * 格式化答案
     */
    private static String formatAnswer(String answer) {
        if (answer.length() == 1) {
            return answer;
        }
        return Arrays.stream(answer.split("")).sorted().collect(Collectors.joining(""));
    }

}
