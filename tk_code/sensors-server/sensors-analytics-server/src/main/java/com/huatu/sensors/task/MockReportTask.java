package com.huatu.sensors.task;

import java.util.ArrayList;
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
import com.huatu.sensors.dao.AnswerCardDao;
import com.huatu.sensors.dao.MatchDao;
import com.huatu.sensors.dao.pandora.MatchUserMetaMapper;
import com.huatu.sensors.entity.MatchUserMeta;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.Match;
import com.sensorsdata.analytics.javasdk.SensorsAnalytics;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;

import lombok.extern.slf4j.Slf4j;
import tk.mybatis.mapper.entity.Example;

/**
 * 模考数据上报到神策
 * 
 * @author zhangchong
 *
 */
@Slf4j
@Component
public class MockReportTask {
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private RedisTemplate sessionRedisTemplate;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private AnswerCardDao answerCardDao;

	@Autowired
	private MatchDao matchDao;

	@Autowired
	private SensorsAnalytics sensorsAnalytics;

	@Autowired
	private MatchUserMetaMapper matchUserMetaMapper;

	@Value("${server.user.url:https://ns.huatu.com}")
	private String userServer;

	private String getUserInfo = "/u/v1/users/batchUserInfo";

	private String getSubjectInfo = "/k/v2/subjects/tree/static";

	/**
	 * 已经上报的大赛id
	 */
	private List<Integer> filterMathList = Lists.newArrayList(4001896, 4001857, 4001849, 4001836, 4001822,4001980,4001984);

	private String reportMatchKey = "report.match.id";

	/**
	 * 凌晨12点半执行
	 */
	@Scheduled(cron = "0 30 0 * * ?")
	public void scheduled() {
		new Thread(() -> {
			log.info("---------------开始扫描前一天已完成模考---------------");
			 List<Match> matches = matchDao.findLastDayComplateMatch();
			//List<Match> matches = matchDao.findComplateMatch();
			log.info("---------------上报模考数量为:{}", matches.size());
			matches.forEach(match -> {
				if (!filterMathList.contains(match.getPaperId())
						&& !sessionRedisTemplate.opsForSet().isMember(reportMatchKey, match.getPaperId()+"")) {
					sessionRedisTemplate.opsForSet().add(reportMatchKey, match.getPaperId()+"");
					log.info("---------------上报模考id:{}", match.getPaperId());
					reportStartAction(match);
					reportEndAction(match);
					reportEnrollAction(match);
				} else {
					log.info("---------------id为:{}的模考已经上报过", match.getPaperId());
				}

			});

		}).start();

	}

	/**
	 * 上报开始答题
	 */
	public void reportStartAction(Match match) {
		Integer matchId = match.getPaperId();
		int subject = match.getSubject();
		Map categoryAndSubject = getCategoryAndSubject(subject);
		MatchUserMeta userMeta = new MatchUserMeta();
		userMeta.setMatchId(matchId);
		Example example = new Example(MatchUserMeta.class);
		example.and().andEqualTo("matchId", matchId).andNotEqualTo("practiceId", -1);
		List<MatchUserMeta> userMetas = matchUserMetaMapper.selectByExample(example);
		log.info("试卷id:{},开始答题人数为:{},", matchId, userMetas.size());
		userMetas.forEach(metaInfo -> {
			Map<String, Object> properties = Maps.newHashMap();
			// 大赛id
			String match_id = metaInfo.getMatchId() + "";
			String match_title = match.getName();
			Boolean is_first_answer = true;
			int count = matchUserMetaMapper.selectCount(MatchUserMeta.builder().userId(metaInfo.getUserId()).build());
			if (count > 1) {
				is_first_answer = false;
			}
			properties.put("match_id", match_id);
			properties.put("match_title", match_title);
			properties.put("is_first_answer", is_first_answer);
			if(categoryAndSubject != null) {
				properties.putAll(categoryAndSubject);
			}

			AnswerCard answerCard = answerCardDao.findById(metaInfo.getPracticeId());

			Date date = new Date(answerCard.getCardCreateTime());
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
					log.info("开始答题上报信息:{}",properties);
					sensorsAnalytics.track(mobile, true, SensorsEventEnum.MOKAO_STARTANSWER.getCode(), properties);
				} catch (InvalidArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		});

	}

	private Map getCategoryAndSubject(int subject) {
		Map<String, Object> properties = Maps.newHashMap();
		//招警机考
		if(100100173 == subject) {
			properties.put("match_subject", "招警机考");
			properties.put("match_class", "招警考试");
		}
		Map map = restTemplate.getForObject("https://ns.huatu.com/k/v2/subjects/tree/static", Map.class);
		List<Map<String, Object>> data = (List<Map<String, Object>>) map.get("data");

		for (Map<String, Object> categoryInfo : data) {
			List<Map<String, Object>> subjectData = (List<Map<String, Object>>) categoryInfo.get("childrens");
			for (Map<String, Object> subjectInfo : subjectData) {
				if ((int) subjectInfo.get("id") == subject) {
					properties.put("match_subject", subjectInfo.get("name"));
					properties.put("match_class", categoryInfo.get("name"));
					return properties;
				}
			}
		}
		return null;

	}

	/**
	 * 上报结束答题
	 * 
	 * @param match
	 */
	public void reportEndAction(Match match) {

		Integer matchId = match.getPaperId();
		int subject = match.getSubject();
		MatchUserMeta userMeta = new MatchUserMeta();
		userMeta.setMatchId(matchId);
		Example example = new Example(MatchUserMeta.class);
		example.and().andEqualTo("matchId", matchId).andNotEqualTo("practiceId", -1);
		List<MatchUserMeta> userMetas = matchUserMetaMapper.selectByExample(example);
		log.info("试卷id:{},交卷答题人数为:{},", matchId, userMetas.size());
		Map categoryAndSubject = getCategoryAndSubject(subject);
		userMetas.forEach(metaInfo -> {
			Map<String, Object> properties = Maps.newHashMap();
			// 大赛id
			String match_id = metaInfo.getMatchId() + "";
			String match_title = match.getName();
			Boolean is_first_answer = true;
			int count = matchUserMetaMapper.selectCount(MatchUserMeta.builder().userId(metaInfo.getUserId()).build());
			if (count > 1) {
				is_first_answer = false;
			}
			properties.put("match_id", match_id);
			properties.put("match_title", match_title);
			if(categoryAndSubject != null) {
				properties.putAll(categoryAndSubject);
			}
			AnswerCard answerCard = answerCardDao.findById(metaInfo.getPracticeId());

			properties.put("match_answer_duration", answerCard.getExpendTime());
			if(metaInfo.getSubmitTime() != null) {
				Date date = new Date(metaInfo.getSubmitTime().getTime());
				properties.put("$time", date);
			}else {
				Date date = new Date(metaInfo.getGmtModify().getTime());
				properties.put("$time", date);
			}

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
					log.info("模考交卷上报信息:{}",properties);
					sensorsAnalytics.track(mobile, true, SensorsEventEnum.MOKAO_ENDANSWER.getCode(), properties);
				} catch (InvalidArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		});

	}

	/**
	 * 上报报名操作
	 * 
	 * @param match
	 */
	public void reportEnrollAction(Match match) {

		Integer matchId = match.getPaperId();
		int subject = match.getSubject();
		Map categoryAndSubject = getCategoryAndSubject(subject);
		MatchUserMeta userMeta = new MatchUserMeta();
		userMeta.setMatchId(matchId);
		List<MatchUserMeta> userMetas = matchUserMetaMapper.select(userMeta);
		log.info("上报人数为：" + userMetas.size());
		userMetas.forEach(metaInfo -> {
			// long practiceId = metaInfo.getPracticeId();
			Map<String, Object> properties = Maps.newHashMap();
			// 报考区域
			String sign_up_city = metaInfo.getPositionName();
			// 大赛id
			String match_id = metaInfo.getMatchId() + "";
			String match_title = match.getName();
			Boolean is_first_sign_up = true;
			int count = matchUserMetaMapper.selectCount(MatchUserMeta.builder().userId(metaInfo.getUserId()).build());
			if (count > 1) {
				is_first_sign_up = false;
			}
			properties.put("sign_up_city", sign_up_city);
			properties.put("match_id", match_id);
			properties.put("match_title", match_title);
			properties.put("is_first_sign_up", is_first_sign_up);
			if(categoryAndSubject != null) {
				properties.putAll(categoryAndSubject);
			}
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
					log.info("模考报名上报信息:{}",properties);
					sensorsAnalytics.track(mobile, true, SensorsEventEnum.MOKAO_SURESIGNUP.getCode(), properties);
				} catch (InvalidArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		});

	}

}
