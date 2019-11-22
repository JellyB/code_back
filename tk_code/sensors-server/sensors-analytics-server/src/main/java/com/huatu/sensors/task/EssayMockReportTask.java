package com.huatu.sensors.task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.consts.SensorsEventEnum;
import com.huatu.sensors.dao.essay.EssayMockMapper;
import com.huatu.sensors.dao.essay.EssayMockUserMetaMapper;
import com.huatu.sensors.dao.essay.EssayPaperAnswerMapper;
import com.huatu.sensors.entity.essay.EssayMockExam;
import com.huatu.sensors.entity.essay.EssayMockUserMeta;
import com.huatu.sensors.entity.essay.EssayPaperAnswer;
import com.huatu.sensors.utils.Dateutils;
import com.sensorsdata.analytics.javasdk.SensorsAnalytics;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;

import lombok.extern.slf4j.Slf4j;
import tk.mybatis.mapper.entity.Example;

/**
 * 申论模考数据上报到神策
 * 
 * @author zhangchong
 *
 */
@Slf4j
@Component
public class EssayMockReportTask {
	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private RedisTemplate sessionRedisTemplate;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private SensorsAnalytics sensorsAnalytics;

	@Value("${server.user.url:https://ns.huatu.com}")
	private String userServer;

	private String getUserInfo = "/u/v1/users/batchUserInfo";

	private String getSubjectInfo = "/k/v2/subjects/tree/static";

	@Autowired
	private EssayMockMapper essayMockMapper;

	@Autowired
	private EssayMockUserMetaMapper essayMockUserMetaMapper;

	@Autowired
	private EssayPaperAnswerMapper essayPaperAnswerMapper;

	/**
	 * 已经上报的大赛id
	 */
	private List<Integer> filterMathList = Lists.newArrayList(740,741);

	private String reportMatchKey = "report.essay.match.id";

	private static Map<Integer, String> areaMap = Maps.newConcurrentMap();

	static {
		areaMap.put(-9, "全国");
		areaMap.put(1, "北京");
		areaMap.put(21, "天津");
		areaMap.put(41, "河北");
		areaMap.put(225, "山西");
		areaMap.put(356, "内蒙古");
		areaMap.put(471, "辽宁");
		areaMap.put(586, "吉林");
		areaMap.put(656, "黑龙江");
		areaMap.put(802, "上海");
		areaMap.put(823, "江苏");
		areaMap.put(943, "浙江");
		areaMap.put(1045, "安徽");
		areaMap.put(1168, "福建");
		areaMap.put(1263, "江西");
		areaMap.put(1374, "山东");
		areaMap.put(1532, "河南");
		areaMap.put(1709, "湖北");
		areaMap.put(1826, "湖南");
		areaMap.put(1963, "广东");
		areaMap.put(1964, "广州");
		areaMap.put(1988, "深圳");
		areaMap.put(2106, "广西");
		areaMap.put(2230, "海南");
		areaMap.put(2257, "重庆");
		areaMap.put(2299, "四川");
		areaMap.put(2502, "贵州");
		areaMap.put(2600, "云南");
		areaMap.put(2746, "西藏");
		areaMap.put(2827, "陕西");
		areaMap.put(2945, "甘肃");
		areaMap.put(3046, "青海");
		areaMap.put(3098, "宁夏");
		areaMap.put(3125, "新疆");
		areaMap.put(3243, "新疆兵团");
		areaMap.put(10017, "警校联考");

	}

	/**
	 * 凌晨12点50执行
	 */
	@Scheduled(cron = "0 50 0 * * ?")
	public void scheduled() {
		new Thread(() -> {
			log.info("---------------开始扫描前一天已完成申论模考---------------");
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			int day = calendar.get(Calendar.DATE);
			calendar.set(Calendar.DATE, day - 1);
			long startTime = Dateutils.getBeginOfDay(calendar.getTime());
			long endTime = Dateutils.getEndOfDay(calendar.getTime());
			Example example = new Example(EssayMockExam.class);
			//example.or().andLessThanOrEqualTo("id", 742);
			example.or().andBetween("endTime", startTime, endTime);
			List<EssayMockExam> mockList = essayMockMapper.selectByExample(example);
			log.info("---------------上报申论模考数量为:{}", mockList.size());
			mockList.forEach(match -> {
				if (!filterMathList.contains(match.getId())
						&& !sessionRedisTemplate.opsForSet().isMember(reportMatchKey, match.getId() + "")) {
					sessionRedisTemplate.opsForSet().add(reportMatchKey, match.getId() + "");
					log.info("---------------上报申论模考id:{}", match.getId());
					reportStartAction(match);
					reportEndAction(match);
					reportEnrollAction(match);
				} else {
					log.info("---------------id为:{}的申论模考已经上报过", match.getId());
				}

			});

		}).start();

	}

	/**
	 * 上报报名操作
	 * 
	 * @param match
	 */
	public void reportEnrollAction(EssayMockExam mock) {

		Long matchId = mock.getId();
		EssayMockUserMeta userMeta = new EssayMockUserMeta();
		userMeta.setPaperId(matchId);
		List<EssayMockUserMeta> userMetaList = essayMockUserMetaMapper.select(userMeta);
		log.info("申论上报人数为：" + userMetaList.size());
		userMetaList.forEach(metaInfo -> {
			Map<String, Object> properties = Maps.newHashMap();

			properties.put("match_subject", "申论");
			properties.put("match_class", "公务员");
			// 报考区域
			Integer positionId = metaInfo.getPositionCount();
			// 大赛id
			String match_id = metaInfo.getPaperId() + "";
			String match_title = mock.getName();
			Boolean is_first_sign_up = true;
			int count = essayMockUserMetaMapper
					.selectCount(EssayMockUserMeta.builder().userId(metaInfo.getUserId()).build());
			if (count > 1) {
				is_first_sign_up = false;
			}
			properties.put("sign_up_city", areaMap.get(positionId));
			properties.put("match_id", match_id);
			properties.put("match_title", match_title);
			properties.put("is_first_sign_up", is_first_sign_up);

			Date date = new Date(metaInfo.getGmtCreate().getTime());
			properties.put("$time", date);

			HttpHeaders headers = new HttpHeaders();
			MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
			headers.setContentType(type);
			headers.add("Accept", MediaType.APPLICATION_JSON.toString());
			List<String> idsList = new ArrayList<String>();
			idsList.add(metaInfo.getUserId() + "");

			HttpEntity<String> request = new HttpEntity<String>(idsList.toString(), headers);

			String userUrl = userServer + getUserInfo;
			Map postForObject = restTemplate.postForObject(userUrl, request, Map.class);
			List<Map> userList = (List<Map>) postForObject.get("data");
			String mobile = (String) userList.get(0).get("mobile");
			if (mobile != null) {
				// 上报开始
				try {
					log.info("申论模考报名上报信息:{}", properties);
					sensorsAnalytics.track(mobile, true, SensorsEventEnum.MOKAO_SURESIGNUP.getCode(), properties);
				} catch (InvalidArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		});

	}

	/**
	 * 上报开始答题
	 * 
	 * @param match
	 */
	public void reportStartAction(EssayMockExam match) {
		Long matchId = match.getId();

		Example example = new Example(EssayPaperAnswer.class);
		example.and().andEqualTo("paperBaseId", matchId);
		List<EssayPaperAnswer> paperAnswerList = essayPaperAnswerMapper.selectByExample(example);
		log.info("试卷id:{},开始答题人数为:{},", matchId, paperAnswerList.size());
		paperAnswerList.forEach(paperAnswer -> {
			Map<String, Object> properties = Maps.newHashMap();
			properties.put("match_subject", "申论");
			properties.put("match_class", "公务员");
			// 大赛id
			String match_title = match.getName();
			Boolean is_first_answer = true;
			int count = essayMockUserMetaMapper
					.selectCount(EssayMockUserMeta.builder().userId(paperAnswer.getUserId()).build());
			if (count > 1) {
				is_first_answer = false;
			}
			properties.put("match_id", matchId);
			properties.put("match_title", match_title);
			properties.put("is_first_answer", is_first_answer);

			Date date = paperAnswer.getGmtCreate();
			properties.put("$time", date);

			HttpHeaders headers = new HttpHeaders();
			MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
			headers.setContentType(type);
			headers.add("Accept", MediaType.APPLICATION_JSON.toString());
			List<String> idsList = new ArrayList<String>();
			idsList.add(paperAnswer.getUserId() + "");

			HttpEntity<String> request = new HttpEntity<String>(idsList.toString(), headers);

			String userUrl = userServer + getUserInfo;
			Map postForObject = restTemplate.postForObject(userUrl, request, Map.class);
			List<Map> userList = (List<Map>) postForObject.get("data");
			String mobile = (String) userList.get(0).get("mobile");
			if (mobile != null) {
				// 上报开始
				try {
					log.info("开始答题上报信息:{}", properties);
					sensorsAnalytics.track(mobile, true, SensorsEventEnum.MOKAO_STARTANSWER.getCode(), properties);
				} catch (InvalidArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		});

	}

	/**
	 * 结束答题
	 * 
	 * @param match
	 */
	public void reportEndAction(EssayMockExam match) {

		Long matchId = match.getId();
		Example example = new Example(EssayPaperAnswer.class);
		example.and().andEqualTo("paperBaseId", matchId).andGreaterThanOrEqualTo("bizStatus", 2);
		List<EssayPaperAnswer> paperAnswerList = essayPaperAnswerMapper.selectByExample(example);
		log.info("试卷id:{},申论交卷答题人数为:{},", matchId, paperAnswerList.size());
		paperAnswerList.forEach(paperAnswer -> {
			Map<String, Object> properties = Maps.newHashMap();
			properties.put("match_subject", "申论");
			String match_title = match.getName();
			Boolean is_first_answer = true;
			int count = essayMockUserMetaMapper
					.selectCount(EssayMockUserMeta.builder().userId(paperAnswer.getUserId()).build());
			if (count > 1) {
				is_first_answer = false;
			}
			properties.put("match_id", matchId);
			properties.put("match_title", match_title);
			properties.put("is_first_answer", is_first_answer);
			properties.put("match_answer_duration", paperAnswer.getSpendTime());
			if (paperAnswer.getSubmitTime() != null) {
				properties.put("$time", paperAnswer.getSubmitTime());
			} else {
				properties.put("$time", paperAnswer.getGmtModify());
			}

			HttpHeaders headers = new HttpHeaders();
			MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
			headers.setContentType(type);
			headers.add("Accept", MediaType.APPLICATION_JSON.toString());
			List<String> idsList = new ArrayList<String>();
			idsList.add(paperAnswer.getUserId() + "");

			HttpEntity<String> request = new HttpEntity<String>(idsList.toString(), headers);

			String userUrl = userServer + getUserInfo;
			Map postForObject = restTemplate.postForObject(userUrl, request, Map.class);
			List<Map> userList = (List<Map>) postForObject.get("data");
			String mobile = (String) userList.get(0).get("mobile");
			if (mobile != null) {
				// 上报开始
				try {
					log.info("申论模考交卷上报信息:{}", properties);
					sensorsAnalytics.track(mobile, true, SensorsEventEnum.MOKAO_ENDANSWER.getCode(), properties);
				} catch (InvalidArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		});

	}

}
