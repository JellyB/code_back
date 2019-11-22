package com.huatu.sensors.service.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.huatu.common.consts.SensorsEventEnum;
import com.huatu.sensors.dao.AnswerCardDao;
import com.huatu.sensors.dao.MatchDao;
import com.huatu.sensors.mq.message.EnrollMatchMessage;
import com.huatu.sensors.mq.message.MockSubmitMessage;
import com.huatu.sensors.service.SensorsService;
import com.huatu.sensors.utils.SensorsUtils;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.Match;
import com.huatu.ztk.paper.bean.StandardCard;
import com.huatu.ztk.paper.common.PositionConstants;
import com.huatu.ztk.user.service.UserSessionService;
import com.sensorsdata.analytics.javasdk.SensorsAnalytics;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;

import lombok.extern.slf4j.Slf4j;

/**
 * 神策埋点业务处理
 * 
 * @author zhangchong
 *
 */
@Service
@Slf4j
public class SensorsServiceImpl implements SensorsService {

	@Autowired
	private MatchDao matchDao;

	@Autowired
	private AnswerCardDao answerCardDao;

	@Autowired
	private UserSessionService userSessionService;

	@Autowired
	private SensorsAnalytics sensorsAnalytics;

	private static Map<Integer, String> categoryMap = Maps.newHashMap();

	static {
		categoryMap.put(1, "公务员");
		categoryMap.put(3, "事业单位");
		categoryMap.put(200100045, "教师招聘");
		categoryMap.put(200100048, "教师资格证");
		categoryMap.put(200100047, "招警考试");
		categoryMap.put(41, "公遴选");
		categoryMap.put(42, "军转");
		categoryMap.put(43, "国家电网");
		categoryMap.put(200100000, "医疗");
		categoryMap.put(200100002, "金融");
		categoryMap.put(100100633, "考研");
		categoryMap.put(200100046, "其他");

	}

	/**
	 * 模考大赛开始答题埋点处理
	 */
	@Async
	public void createMatchAnswerCardAnalytics(String token, int id, int subject, int terminal) {

		try {
			// 华图在线_app_pc_华图在线_模考大赛开始答题 $预置属性
			// is_first_answer 是否首次答题 BOOL
			// exam_type 考试类型 字符串 国考，省考
			// match_subject 考试科目 字符串 申论
			// match_class 考试类属 字符串 公务员，教师
			// match_id 大赛ID 字符串
			// match_title 大赛主题 字符串
			log.info("createMatchAnswerCardAnalytics start subject is {}", subject);
			Map<String, Object> properties = Maps.newHashMap();
			long userId = userSessionService.getUid(token);
			int category = userSessionService.getCatgory(token);
			String ucId = userSessionService.getUcId(token);
			Match match = matchDao.findById(id);
			if (match != null) {
				int tag = match.getTag();
				String matchSubject = "行测";
				if (3 == tag) {
					matchSubject = "申论";
				}
				// 查询考生参赛记录
				long mockCount = answerCardDao.countMockByUidAndSubject(userId);
				log.info("uId:{} countMockByUidAndSubject is:{}", userId, mockCount);
				boolean isFirstAnswer = true;
				if (mockCount > 0) {
					// 参加过该科目下的考试
					isFirstAnswer = false;
				}
				// 查询结束
				String categoryStr = categoryMap.get(category);
				properties.put("is_first_answer", isFirstAnswer);
				properties.put("match_class", categoryStr);
				properties.put("match_id", String.valueOf(match.getPaperId()));
				properties.put("match_title", String.valueOf(match.getName()));
				properties.put("exam_type", SensorsUtils.getExamType(tag));
				properties.put("match_subject", matchSubject);
				properties.put("platform", SensorsUtils.getPlatform(terminal));
				log.info("createMatchAnswerCardAnalytics properties={}", properties.toString());
				sensorsAnalytics.track(ucId, true, SensorsEventEnum.MOKAO_STARTANSWER.getCode(), properties);
				sensorsAnalytics.flush();
			} else {
				log.warn("not exist match id:{}", id);
			}

		} catch (InvalidArgumentException e) {
			log.error("sa track error:" + e);
		}
	}

	/**
	 * 模考交卷埋点处理
	 */
	@Override
	public void submitMatchAnswerCardAnalytics(MockSubmitMessage msg) {
		/**
		 * $预置属性 exam_type 考试类型 字符串 国考，省考 match_subject 考试科目 字符串 申论 match_class 考试类属 字符串
		 * 公务员，教师 match_id 大赛ID 字符串 match_title 大赛主题 字符串 match_answer_duration 答题时长 数值
		 */
		try {
			log.info("submitMatchAnswerCardAnalytics start userId is {}", msg.getUserId());
			Map<String, Object> properties = Maps.newHashMap();
			String ucId = userSessionService.getUcId(msg.getToken());
			int category = userSessionService.getCatgory(msg.getToken());
			// 获取答题卡信息
			AnswerCard answerCard = answerCardDao.findById(msg.getPracticeId());
			// 获取大赛信息
			int paperId = ((StandardCard) answerCard).getPaper().getId();
			log.info("paperId is:{}", paperId);
			Match match = matchDao.findById(paperId);
			int tag = match.getTag();
			String matchSubject = "行测";
			if (3 == tag) {
				matchSubject = "申论";
			}
			String categoryStr = categoryMap.get(category);
			properties.put("exam_type", SensorsUtils.getExamType(tag));
			properties.put("match_subject", matchSubject);
			properties.put("match_class", categoryStr);
			properties.put("match_id", String.valueOf(paperId));
			properties.put("match_title", String.valueOf(match.getName()));
			properties.put("match_answer_duration", answerCard.getExpendTime());
			properties.put("platform", SensorsUtils.getPlatform(msg.getTerminal()));
			log.info("submitMatchAnswerCardAnalytics properties={}", properties.toString());
			sensorsAnalytics.track(ucId, true, SensorsEventEnum.MOKAO_ENDANSWER.getCode(), properties);
			sensorsAnalytics.flush();
		} catch (InvalidArgumentException e) {
			log.error("sa track error:{}", e);
		}
	}

	/**
	 * 模考确认报名
	 */
	@Override
	public void enrollMatchAnalytics(EnrollMatchMessage msg) {
		/**
		 * is_first_sign_up 是否首次报名 BOOL sign_up_city 报考区域 字符串 match_id 大赛ID 字符串
		 * match_title 大赛主题 字符串 exam_type 考试类型 字符串 国考，省考 match_subject 考试科目 字符串 申论
		 * match_class 考试类属 字符串 公务员，教师
		 */
		try {
			log.info("enrollMatchAnalytics start userId is {}", msg.getUserId());
			Map<String, Object> properties = Maps.newHashMap();
			String ucId = userSessionService.getUcId(msg.getToken());
			int category = userSessionService.getCatgory(msg.getToken());

			log.info("matchId is:{}", msg.getMatchId());
			Match match = matchDao.findById(msg.getMatchId());
			if (match != null) {
				int tag = match.getTag();
				String matchSubject = "行测";
				if (3 == tag) {
					matchSubject = "申论";
				}
				String categoryStr = categoryMap.get(category);
				// 是否首次报名
				long count = matchDao.countMatchUserMetaByUid(msg.getUserId());
				log.info("用户id :{} 报名次数:{}", msg.getUserId(), count);
				boolean isFirstsignUp = false;
				if (count == 0) {
					isFirstsignUp = true;
				}
				properties.put("is_first_sign_up", isFirstsignUp);
				// 报考区域
				properties.put("sign_up_city", PositionConstants.getFullPositionName(msg.getPositionId()));
				properties.put("match_id", String.valueOf(msg.getMatchId()));
				properties.put("match_title", String.valueOf(match.getName()));
				properties.put("exam_type", SensorsUtils.getExamType(tag));
				properties.put("match_subject", matchSubject);
				properties.put("match_class", categoryStr);
				properties.put("platform", SensorsUtils.getPlatform(msg.getTerminal()));
				log.info("enrollMatchAnalytics properties={}", properties.toString());
				sensorsAnalytics.track(ucId, true, SensorsEventEnum.MOKAO_SURESIGNUP.getCode(), properties);
				sensorsAnalytics.flush();
			} else {
				log.error("not exist match id:{}", msg.getMatchId());
			}
		} catch (InvalidArgumentException e) {
			log.error("sa track error:{}", e);
		}

	}

}
