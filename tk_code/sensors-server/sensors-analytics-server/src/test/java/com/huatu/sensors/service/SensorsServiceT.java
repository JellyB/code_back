package com.huatu.sensors.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.annotation.Resource;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import com.google.common.collect.Maps;
import com.huatu.common.consts.SensorsEventEnum;
import com.huatu.sensors.AppTest;
import com.huatu.sensors.dao.AnswerCardDao;
import com.huatu.sensors.dao.MatchDao;
import com.huatu.sensors.dao.essay.EssayMockMapper;
import com.huatu.sensors.dao.pandora.MatchUserMetaMapper;
import com.huatu.sensors.entity.MatchUserMeta;
import com.huatu.sensors.entity.essay.EssayMockExam;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.Match;
import com.huatu.ztk.user.service.UserSessionService;
import com.sensorsdata.analytics.javasdk.SensorsAnalytics;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;

import lombok.extern.slf4j.Slf4j;
import tk.mybatis.mapper.entity.Example;

@Slf4j
public class SensorsServiceT extends AppTest {

	@Autowired
	private SensorsService sensorsService;

	@Autowired
	private UserSessionService userSessionService;

	@Resource(name = "sessionRedisTemplate")
	private RedisTemplate<String, String> sessionRedisTemplate;

	@Autowired
	private MatchUserMetaMapper matchUserMetaMapper;

	@Autowired
	private AnswerCardDao answerCardDao;

	@Autowired
	private MatchDao matchDao;

	@Autowired
	private SensorsAnalytics sensorsAnalytics;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private EssayMockMapper essayMockMapper;

	@Test
	public void createMatchAnswerCardAnalytics() {
		sensorsService.createMatchAnswerCardAnalytics("4bbd1dc73ca14e8ebf34b8ec49ba0af4", 3526734, 100100262, 1);

	}

	@Test
	public void sessionT() {
		ValueOperations<String, String> opeations = sessionRedisTemplate.opsForValue();
		opeations.set("test_name", "change");

	}

	/**
	 * 报名信息上报
	 */
	@Test
	public void reportMatchInfo(){
		Integer matchId = 4001909;
		Match match = matchDao.findById(matchId);
		MatchUserMeta userMeta = new MatchUserMeta();
		// userMeta.setPaperId(4001822);
		userMeta.setMatchId(matchId);
		List<MatchUserMeta> userMetas = matchUserMetaMapper.select(userMeta);
		System.out.println("上报人数为："+userMetas.size());
		userMetas.forEach(metaInfo -> {
			// long practiceId = metaInfo.getPracticeId();
			Map<String, Object> properties = Maps.newHashMap();
			// 报考区域
			String sign_up_city = metaInfo.getPositionName();
			// 大赛id
			String match_id = metaInfo.getMatchId() + "";
			String match_title = match.getName();
			String match_subject = "行测";
			String match_class = "公务员";
			Boolean is_first_sign_up = true;
			int count = matchUserMetaMapper.selectCount(MatchUserMeta.builder().userId(metaInfo.getUserId()).build());
			if (count > 1) {
				is_first_sign_up = false;
			}
			properties.put("sign_up_city", sign_up_city);
			properties.put("match_id", match_id);
			properties.put("match_title", match_title);
			properties.put("match_subject", match_subject);
			properties.put("match_class", match_class);
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

			String userUrl = "https://ns.huatu.com/u/v1/users/batchUserInfo";
			//String userUrl = "http://123.103.86.52/u/v1/users/batchUserInfo";
			// {data=[{id=237489, mobile=13552839928, email=changchang1021@126.com,
			// name=htwx0742, nick=htwx0742, signature=, area=0, subject=1, status=1,
			// avatar=http://tiku.huatu.com/cdn/images/vhuatu/avatars/default.png,
			// regFrom=1, deviceToken=null, ucenterId=237489}], code=1000000}
			Map postForObject = restTemplate.postForObject(userUrl, request, Map.class);
			List<Map> userList = (List<Map>) postForObject.get("data");
			String mobile = (String) userList.get(0).get("mobile");
			if(mobile != null) {
				//上报开始
				try {
					sensorsAnalytics.track(mobile, true, SensorsEventEnum.MOKAO_SURESIGNUP.getCode(), properties);
				} catch (InvalidArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		});

	}
	
	
	/**
	 * 开始答题上报
	 */
	@Test
	public void reportStartInfo(){
		Integer matchId = 4001909;
		Match match = matchDao.findById(matchId);
		MatchUserMeta userMeta = new MatchUserMeta();
		// userMeta.setPaperId(4001822);
		userMeta.setMatchId(matchId);
		Example example =  new Example(MatchUserMeta.class);
		example.and().andEqualTo("matchId", matchId).andNotEqualTo("practiceId", -1);
		List<MatchUserMeta> userMetas = matchUserMetaMapper.selectByExample(example);
		System.out.println("开始答题人数为："+userMetas.size());
		userMetas.forEach(metaInfo -> {
			// long practiceId = metaInfo.getPracticeId();
			Map<String, Object> properties = Maps.newHashMap();
			// 大赛id
			String match_id = metaInfo.getMatchId() + "";
			String match_title = match.getName();
			String match_subject = "行测";
			String match_class = "公务员";
			Boolean is_first_answer = true;
			long count = answerCardDao.countMockByUidAndSubject(metaInfo.getUserId().longValue());
			if (count > 1) {
				is_first_answer = false;
			}
			properties.put("match_subject", match_subject);
			properties.put("match_class", match_class);
			properties.put("match_id", match_id);
			properties.put("match_title", match_title);
			properties.put("is_first_answer", is_first_answer);
			
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

			String userUrl = "https://ns.huatu.com/u/v1/users/batchUserInfo";
			//String userUrl = "http://123.103.86.52/u/v1/users/batchUserInfo";
			// {data=[{id=237489, mobile=13552839928, email=changchang1021@126.com,
			// name=htwx0742, nick=htwx0742, signature=, area=0, subject=1, status=1,
			// avatar=http://tiku.huatu.com/cdn/images/vhuatu/avatars/default.png,
			// regFrom=1, deviceToken=null, ucenterId=237489}], code=1000000}
			Map postForObject = restTemplate.postForObject(userUrl, request, Map.class);
			List<Map> userList = (List<Map>) postForObject.get("data");
			String mobile = (String) userList.get(0).get("mobile");
			if(mobile != null) {
				//上报开始
				try {
					sensorsAnalytics.track(mobile, true, SensorsEventEnum.MOKAO_STARTANSWER.getCode(), properties);
				} catch (InvalidArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		});

	}

	/**
	 * 上报交卷
	 */
	@Test
	public void reportEndInfo(){
		Integer matchId = 4001909;
		Match match = matchDao.findById(matchId);
		MatchUserMeta userMeta = new MatchUserMeta();
		// userMeta.setPaperId(4001822);
		userMeta.setMatchId(matchId);
		Example example =  new Example(MatchUserMeta.class);
		example.and().andEqualTo("matchId", matchId).andNotEqualTo("practiceId", -1);
		List<MatchUserMeta> userMetas = matchUserMetaMapper.selectByExample(example);
		System.out.println("交卷人数为："+userMetas.size());
		userMetas.forEach(metaInfo -> {
			// long practiceId = metaInfo.getPracticeId();
			Map<String, Object> properties = Maps.newHashMap();
			// 大赛id
			String match_id = metaInfo.getMatchId() + "";
			String match_title = match.getName();
			String match_subject = "行测";
			String match_class = "公务员";
			Boolean is_first_answer = true;
			long count = answerCardDao.countMockByUidAndSubject(metaInfo.getUserId().longValue());
			if (count > 1) {
				is_first_answer = false;
			}
			properties.put("match_subject", match_subject);
			properties.put("match_class", match_class);
			properties.put("match_id", match_id);
			properties.put("match_title", match_title);
			
			AnswerCard answerCard = answerCardDao.findById(metaInfo.getPracticeId());
			
			properties.put("match_answer_duration",answerCard.getExpendTime() );
			Date date = new Date(metaInfo.getSubmitTime().getTime());
			properties.put("$time", date);
			
			HttpHeaders headers = new HttpHeaders();
			MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
			headers.setContentType(type);
			headers.add("Accept", MediaType.APPLICATION_JSON.toString());
			List<String> idsList = new ArrayList<String>();
			idsList.add(metaInfo.getUserId() + "");

			HttpEntity<String> request = new HttpEntity<String>(idsList.toString(), headers);

			String userUrl = "https://ns.huatu.com/u/v1/users/batchUserInfo";
			//String userUrl = "http://123.103.86.52/u/v1/users/batchUserInfo";
			// {data=[{id=237489, mobile=13552839928, email=changchang1021@126.com,
			// name=htwx0742, nick=htwx0742, signature=, area=0, subject=1, status=1,
			// avatar=http://tiku.huatu.com/cdn/images/vhuatu/avatars/default.png,
			// regFrom=1, deviceToken=null, ucenterId=237489}], code=1000000}
			Map postForObject = restTemplate.postForObject(userUrl, request, Map.class);
			List<Map> userList = (List<Map>) postForObject.get("data");
			String mobile = (String) userList.get(0).get("mobile");
			if(mobile != null) {
				//上报开始
				try {
					sensorsAnalytics.track(mobile, true, SensorsEventEnum.MOKAO_ENDANSWER.getCode(), properties);
				} catch (InvalidArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		});

	}
	
	@Test
	public void send(){
		String string = "318690,318710,318712,318713,318725,318726,318734,318749,318838,318913,318932,318934,318973,318981,318983,318994,319034,319047,319059,319087,319093,319115,319149,319173,319185,319199,319222,319236,319262,319276,319284,319288,319297,319317,319327,319336,319341,319352,319353,319364,319365,319374,319418,319432,319491,319495,319502,319507,319516,319520,319524,319529,319565,319566,319569,319577,319584,319591,319603,319619,319621,319628,319635,319655,319663,319667,319674,319705,319709,319713,319736,319742,319752,319764,319766,319805,319807,319812,319820,319828,319831,319833,319840,319856,319866,319867,319874,319875,319890,319898,319923,319948,319959,319966,319976,319977,319985,319994,320002,320006,320007,320013,320021,320028,320033,320037,320039,320051,320052,320057,320060,320062,320066,320068,320073,320081,320083,320084,320086,320088,320093,320098,320105,320115,320116,320121,320124,320125,320126,320138,320147,320153,320161,320163,320176,320190,320191,320197,320204,320206,320207,320211,320213,320215,320216,320229,320231,320232,320233,320236,320237,320240,320241,320253,320264,320268,320273,320275,320282,320287,320291,320294,320299,320304,320345,320353,320355,320359,320360,320369";
		String userUrl = "https://ns.huatu.com/cr/correct/essayCorrect";
		Arrays.asList(string.split(",")).forEach(id->{
			Object postForObject = restTemplate.getForObject(userUrl+"?answerCardId="+id+"&type=1", Object.class);
			System.out.println(postForObject.toString());
		});
	}
	
	@Test
	public void SyncUserName2PHP() {
		String url = "http://192.168.199.161:8019/lumenapi/v5/c/order/update_user";
		String path = "/Users/zhangchong/Downloads/user4.log";
		try (Stream<String> stream = Files.lines(Paths.get(path))) {
			stream.forEach(line -> {
				String[] split = line.split("本地用户:");
				String temp = split[1];
				String[] split2 = temp.split(" ");
				String localUname = split2[0];
				String temp2 = split2[1];
				String[] split3 = temp2.split("修改用户名为:");
				String newName = split3[1];
				System.out.println(localUname + "----->" + newName);
				//String finalUrl = url + "?newName=" + newName + "&oldName=" + localUname;
				//Object forObject = restTemplate.getForObject(finalUrl, Object.class);
				//System.out.println(forObject.toString());
				String userUrl = "http://192.168.100.115:11453/u/v1/users/delSessionByUname?uname=" + newName;
				Object delTokenRet = restTemplate.getForEntity(userUrl, Object.class);
				System.out.println(delTokenRet);
			});

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * 同步咨询记录
	 */
	@Test
	public void syncZXJL() {
		String url = "http://192.168.199.161:8019/lumenapi/v5/c/order/update_all_user";
		String path = "/Users/zhangchong/Downloads/correct4.log";
		try (Stream<String> stream = Files.lines(Paths.get(path))) {
			stream.forEach(line -> {
				String[] split = line.split("本地用户:");
				String temp = split[1];
				String[] split2 = temp.split(" ");
				String localUname = split2[0];
				String temp2 = split2[1];
				String[] split3 = temp2.split("修改用户名为:");
				String newName = split3[1];
				System.out.println(localUname + "----->" + newName);
				String finalUrl = url + "?newName=" + newName + "&oldName=" + localUname;
				Object forObject = restTemplate.getForObject(finalUrl, Object.class);
				System.out.println(forObject.toString());
			});

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * 多数据源测试
	 */
	@Test
	public void testMutiDatasource(){
		List<MatchUserMeta> selectAll = matchUserMetaMapper.selectAll();
		log.info("行测模考:{}",selectAll.size());
		List<EssayMockExam> selectAll2 = essayMockMapper.selectAll();
		log.info("申论模考:{}",selectAll2.size());
	}
	
}
