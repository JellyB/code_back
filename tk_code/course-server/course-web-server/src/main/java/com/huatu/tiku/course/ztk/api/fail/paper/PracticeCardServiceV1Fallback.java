package com.huatu.tiku.course.ztk.api.fail.paper;

import java.util.HashMap;
import java.util.List;

import com.netflix.hystrix.HystrixCommand;
import feign.hystrix.Fallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.util.ResponseUtil;
import com.huatu.tiku.course.util.ZTKResponseUtil;
import com.huatu.tiku.course.ztk.api.v1.paper.PracticeCardServiceV1;

/**
 * Created by lijun on 2018/6/22
 */
@Component
@Slf4j
public class PracticeCardServiceV1Fallback implements Fallback<PracticeCardServiceV1> {
	@Override
	public PracticeCardServiceV1 create(Throwable throwable, HystrixCommand command) {
		return new PracticeCardServiceV1() {
			@Override
			public Object createCourseExercisesPracticeCard(Integer terminal, Integer subject, Integer uid, String name,
															Integer courseType, Long courseId, String questionId) {
				log.error("PracticeCardServiceV1 createCourseExercisesPracticeCard failed,terminal:{},subject:{},uid:{},name:{},courseType:{},courseId:{}, questionId,:{}, fall back reason:{}", terminal, subject, uid, name, courseType, courseId, questionId, throwable);
				return ZTKResponseUtil.defaultResult();
			}

			@Override
			public Object createCourseBreakPointPracticeCard(Integer terminal, Integer subject, Integer uid, String name,
															 Integer courseType, Long courseId, String questionId, List<Object> questionInfoList) {
				log.error("PracticeCardServiceV1 createCourseBreakPointPracticeCard failed,terminal:{}, subject:{}, uid:{}, name:{},courseType:{}, courseId:{},questionId:{},questionInfoList:{},fall back reason:{}", terminal, subject, uid, name, courseType, courseId, questionId, questionInfoList, throwable);
				return ZTKResponseUtil.defaultResult();
			}

			@Override
			public Object getCourseExercisesCardInfo(long userId, List<HashMap<String, Object>> paramsList) {
				log.error("PracticeCardServiceV1 getCourseExercisesCardInfo failed, userId:{}, paramsList:{}, fall back reason:{}", userId, paramsList, throwable);
				return ZTKResponseUtil.defaultResult();
			}

			@Override
			public Object getCourseBreakPointCardInfo(long userId, List<HashMap<String, Object>> paramsList) {
				log.error("PracticeCardServiceV1 getCourseBreakPointCardInfo failed, userId:{}, paramsList:{}, fall back reason:{}",userId, paramsList, throwable);
				return ZTKResponseUtil.defaultResult();
			}

			/**
			 * 根据id获取答题卡信息
			 *
			 * @param token
			 * @param terminal
			 * @param id
			 * @return
			 */
			@Override
			public NetSchoolResponse getAnswerCard(String token, int terminal, long id) {
				log.error("PracticeCardServiceV1 getAnswerCard failed, token:{}, terminal:{}, id:{}", token, terminal, id, throwable);
				return ResponseUtil.DEFAULT_PAGE_EMPTY;
			}

			@Override
			public Object createAndSaveAnswerCoursePracticeCard(Integer uid, String name,
																Integer courseType, Long courseId, String questionIds, String[] answers, int[] corrects, int[] times) {
				log.error("PracticeCardServiceV1 createAndSaveAnswerCoursePracticeCard failed, uid:{},name:{}, courseType:{},courseId:{},questionIds:{},answers:{},corrects:{},times:{}", uid, name, courseType, courseId, questionIds, answers, corrects, times, throwable);
				return ZTKResponseUtil.defaultResult();
			}

			/**
			 * 批量获取随堂练习报告状态
			 *
			 * @param userId
			 * @param paramsList
			 * @return
			 */
			@Override
			public Object getClassExerciseStatus(int userId, List<HashMap<String, Object>> paramsList) {
				log.error("PracticeCardServiceV1 getClassExerciseStatus failed, userId:{}, paramsList:{}, fall back reason:{},", userId, paramsList, throwable);
				return ResponseUtil.DEFAULT_PAGE_EMPTY;
			}

			/**
			 * 批量查询课后作业答题卡
			 *
			 * @param ids
			 * @return
			 */
			@Override
			public Object getCourseExercisesCardInfoBatch(String ids)
			{
				log.error("PracticeCardServiceV1 getCourseExercisesCardInfoBatch failed, ids:{}, fall back reason:{}",ids, throwable);
				return ResponseUtil.DEFAULT_PAGE_EMPTY;
			}

			/**
			 * 获取随堂练习报告
			 *
			 * @param courseId
			 * @param playType
			 * @param userId
			 * @return
			 */
			@Override
			public NetSchoolResponse getClassExerciseReport(long courseId, int playType, int userId) {
				log.error("PracticeCardServiceV1 getClassExerciseReport failed: courseId:{}, playType:{}, userId:{}, fall back reason:{}", courseId, playType, userId, throwable);
				return ResponseUtil.DEFAULT_PAGE_EMPTY;
			}

			/**
			 * 查询指定用户所有答题卡信息
			 *
			 * @param userId
			 */
			@Override
			public Object getCourseExercisesAllCardInfo(long userId) {
				log.error("PracticeCardServiceV1 getCourseExercisesAllCardInfo failed: userId:{}, fall back reason:{}",userId, throwable);
				return ResponseUtil.DEFAULT_PAGE_EMPTY;
			}

			/**
			 * 查询课后练习答题卡信息V2
			 *
			 * @param cardIds
			 */
			@Override
			public Object getCourseExercisesCardInfoV2(List<Long> cardIds) {
				log.error("PracticeCardServiceV1 getCourseExercisesCardInfoV2 failed, cardIds:{}, fall back reason:{}", cardIds, throwable);
				return ZTKResponseUtil.defaultResult();
			}
		};
	}
}
