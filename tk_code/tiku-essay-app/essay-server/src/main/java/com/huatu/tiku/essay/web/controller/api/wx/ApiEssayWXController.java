package com.huatu.tiku.essay.web.controller.api.wx;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import com.huatu.tiku.essay.entity.UserDto;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.util.ResponseMsg;
import org.apache.commons.collections.MapUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.ErrorResult;
import com.huatu.common.SuccessMessage;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.cache.RedisKeyConstant;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.entity.EssayMockUserMeta;
import com.huatu.tiku.essay.entity.EssayPaperAnswer;
import com.huatu.tiku.essay.entity.EssayPaperBase;
import com.huatu.tiku.essay.repository.EssayMockUserMetaRepository;
import com.huatu.tiku.essay.repository.EssayPaperAnswerRepository;
import com.huatu.tiku.essay.service.EssayEduService;
import com.huatu.tiku.essay.service.EssayFileService;
import com.huatu.tiku.essay.service.EssayPaperService;
import com.huatu.tiku.essay.service.EssayWeiXinService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.vo.resp.FileResultVO;
import com.huatu.tiku.essay.vo.statistics.MockUserVO;

import lombok.extern.slf4j.Slf4j;

/**
 * 给小工具提供的接口
 * 
 * @author zhangchong
 *
 */
@RestController
@RequestMapping("api/wxApi")
@Slf4j
public class ApiEssayWXController {

	@Autowired
	EssayEduService essayEduService;
	@Autowired
	EssayPaperService essayPaperService;
	@Autowired
	RedisTemplate redisTemplate;

	@Autowired
	RestTemplate restTemplate;
	@Autowired
	private EssayFileService essayFileService;
	@Autowired
	private EssayPaperAnswerRepository essayPaperAnswerRepository;
	@Autowired
	private EssayWeiXinService essayWeiXinService;
	
	@Autowired
	private EssayMockUserMetaRepository essayMockUserMetaRepository;
	
	/**
	 * 用户信息地址
	 */
	@Value("${user-web-server}")
	private String userInfoUrl;

	// 试卷算法缓存前缀
	public static String QUESTIONCACAHEPREFIX = "essay_standard_answer_V1_";

	// 白名单key
	public static String WHITE_PAPER_KEY = "white_paper_list_10000";

	public static String WX_ACCESS_TOKEN = "wx_access_token";

	public static String AUTH_URL = "https://api.weixin.qq.com/sns/jscode2session?appid=wxad907804efa41a60&secret=7fad566e1a634aef422f249c11995e06&grant_type=authorization_code&js_code=${code}";

	public static String GETUSERINFO_URL = "https://api.weixin.qq.com/cgi-bin/user/info?access_token=${ACCESS_TOKEN}&openid=${OPENID}&lang=zh_CN";

	public static String GET_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=wxad907804efa41a60&secret=7fad566e1a634aef422f249c11995e06";

	public static String GETUSERINFO_URL2 = "https://api.weixin.qq.com/cgi-bin/user/info?access_token=${ACCESS_TOKEN}&openid=${OPENID}&lang=zh_CN";

	public static String PWD = "huatu2019888";
	
	public final String userUrl = "https://ns.huatu.com/u/v1/users/batchUserInfo";

	/**
	 * 获取openid
	 * 
	 * @param code
	 * @return
	 */
	@LogPrint
	@GetMapping(value = "/getOpenId", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public Object clealQuestionCache(@RequestParam("code") String code) {
		Assert.notNull(code, "code不能为空");
		String ret = restTemplate.getForObject(AUTH_URL.replace("${code}", code), String.class);
		JSONObject json = JSONObject.parseObject(ret);
		log.info("openid is:{}", json.get("openid"));
		return json.get("openid");
	}

	/**
	 * 检测用户权限
	 * 
	 * @param appId
	 * @return
	 */
	@LogPrint
	@GetMapping(value = "/checkUser", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public Object checkUser(@RequestParam("appId") String appId) {
		Assert.notNull(appId, "appId不能为空");
		String url = GETUSERINFO_URL.replace("${OPENID}", appId).replace("${ACCESS_TOKEN}", getAccessToken());
		Map userMap = restTemplate.getForObject(url, Map.class);
		log.info("微信用户信息:{}", userMap.toString());
		return userMap;
	}

	/**
	 * 
	 * @return
	 */
	private String getAccessToken() {
		String access_token = (String) redisTemplate.opsForValue().get(WX_ACCESS_TOKEN);
		if (access_token == null) {
			Map forObject = restTemplate.getForObject(GET_ACCESS_TOKEN_URL, Map.class);
			access_token = (String) forObject.get("access_token");
			redisTemplate.opsForValue().set(WX_ACCESS_TOKEN, access_token);
			redisTemplate.expire(WX_ACCESS_TOKEN, 5000, TimeUnit.SECONDS);
		}
		return access_token;
	}

	/**
	 * 清除试题算法缓存
	 * 
	 * @return
	 */
	@LogPrint
	@PostMapping(value = "/question/clearCache/{detailId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public Object clealQuestionCache(@PathVariable("detailId") Long detailId) {
		Assert.notNull(detailId, "试题详情id不能为空");
		redisTemplate.delete(QUESTIONCACAHEPREFIX + detailId);
		return SuccessMessage.create();
	}

	/**
	 * 添加白名单试卷
	 * 
	 * @param paperId
	 * @return
	 */
	@LogPrint
	@PostMapping(value = "/paper/white/{paperId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public Object addWhitePaper(@PathVariable("paperId") Long paperId) {
		Assert.notNull(paperId, "试卷id不能为空");

		// 根据试卷id查询试卷base信息
		EssayPaperBase paper = essayPaperService.findPaperInfoById(paperId);

		if (null == paper) {
			log.warn("试卷id错误，不存在对应试卷。paperId：{}", paperId);
			throw new BizException(EssayErrors.PAPER_NOT_EXIST);
		}
		redisTemplate.opsForSet().add(WHITE_PAPER_KEY, paperId);
		return SuccessMessage.create();
	}

	/**
	 * 删除白名单试卷
	 * 
	 * @param paperId
	 * @return
	 */
	@LogPrint
	@DeleteMapping(value = "/paper/white/{paperId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public Object deleteWhitePaper(@PathVariable("paperId") Long paperId) {
		Assert.notNull(paperId, "试卷id不能为空");
		redisTemplate.opsForSet().remove(WHITE_PAPER_KEY, paperId);
		return SuccessMessage.create();
	}

	/**
	 * 生成试卷pdf
	 * 
	 * @param questionBaseId
	 * @param paperId
	 * @param questionAnswerId
	 * @param paperAnswerId
	 * @return
	 */
	@LogPrint
	@GetMapping(value = "getMockPaperPdf", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public FileResultVO file(@RequestParam(name = "questionBaseId", defaultValue = "0") Long questionBaseId,
			@RequestParam(name = "paperId", defaultValue = "0") Long paperId,
			@RequestParam(name = "questionAnswerId", defaultValue = "0") Long questionAnswerId,
			@RequestParam(name = "paperAnswerId", defaultValue = "0") Long paperAnswerId) {

		log.info("questionBaseId: {},paperId ：{}，questionAnswerId ：{}，paperAnswerId ：{}", questionBaseId, paperId,
				questionAnswerId, paperAnswerId);
		FileResultVO fileResultVO = essayFileService.saveFile(questionBaseId, paperId, questionAnswerId, paperAnswerId);

		return fileResultVO;

	}
	
	/**
	 * 获取模考redis排名信息
	 * @param paperId
	 * @return
	 */
	@GetMapping(value = "/getMockRankInfo", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public Object getMockRankInfo(Long paperId) {
		String essayUserScoreKey = RedisKeyConstant.getEssayUserScoreKey(paperId);
		Set<TypedTuple<Object>> reverseRangeByScoreWithScores = redisTemplate.opsForZSet()
				.reverseRangeByScoreWithScores(essayUserScoreKey, 0, 100);
		StringBuilder sb = new StringBuilder();
		List<String> rankinfo = Lists.newArrayList();
		List<EssayPaperAnswer> paperAnswers = essayPaperAnswerRepository.findByPaperBaseIdAndStatus(paperId, 1);
		Map<Integer, Long> userMetaMap = Maps.newHashMap();
		paperAnswers.forEach(paperanswer -> {
			userMetaMap.put(paperanswer.getUserId(), paperanswer.getAreaId());
		});
		int count = 0;
		Iterator<TypedTuple<Object>> iterator = reverseRangeByScoreWithScores.iterator();
		while (iterator.hasNext()) {
			count++;
			TypedTuple<Object> next = iterator.next();
			Long areaId = userMetaMap.get(Integer.valueOf(next.getValue().toString()));
			String essayUserAreaScoreKey = RedisKeyConstant.getEssayUserAreaScoreKey(paperId, areaId);
			Long areaRank = redisTemplate.opsForZSet().reverseRank(essayUserAreaScoreKey, next.getValue().toString());
			String info = "rank:" + count + " uid:" + next.getValue() + " score:" + next.getScore() + " areaRank:"
					+ areaRank + 1;
			rankinfo.add(info);
		}
		return rankinfo;
	}
	
	/**
	 * 对比redis和mysql数据
	 * @param paperId
	 * @return
	 */
	@GetMapping(value = "/compareRedisAndMysqlMockInfo", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public Object compareRedisAndMysqlMockInfo(Long paperId) {
		return essayWeiXinService.compareRedisAndMysqlMockInfo(paperId);
	}

	/**
	 * 绑定模考
	 * 
	 * @param paperId
	 * @param mockId
	 * @return
	 */
	@LogPrint
	@PostMapping(value = "bindPaper2Mock", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public Object bindPaper2Mock(@RequestParam(name = "paperId", defaultValue = "0") Long paperId,
			@RequestParam(name = "mockId", defaultValue = "0") Long mockId, String pwd, String nickName) {
		log.info("nickname:{},pwd:{}",nickName,pwd);
		if (!(PWD+nickName).equals(pwd)) {
			throw new BizException(ErrorResult.create(1000001, "2次口令验证失败！"));
		}
		log.info("用户名:{},绑定试题id:{},到模考卷:{} ", nickName, paperId, mockId);
		essayWeiXinService.bindPaper2Mock(paperId, mockId);
		return SuccessMessage.create("绑定成功");
	}
	
	/**
	 * 导出申论考试信息
	 * @param paperId
	 * @param response
	 */
	@GetMapping(value = "downloadMockInfo")
	public void downLoadMockInfo(long paperId, HttpServletResponse response) {

		HttpHeaders headers = new HttpHeaders();
		MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
		headers.setContentType(type);
		headers.add("Accept", MediaType.APPLICATION_JSON.toString());
		Map<Integer, EssayPaperAnswer> paperAnswerMap = Maps.newHashMap();
		List<EssayMockUserMeta> mockUserList = essayMockUserMetaRepository.findByPaperIdAndStatus(paperId, 1);
		List<EssayPaperAnswer> paperAnswerList = essayPaperAnswerRepository
				.findByPaperBaseIdAndStatusAndAnswerCardTypeAndBizStatus(paperId, 1, EssayAnswerCardEnum.ModeTypeEnum.NORMAL.getType(),3);
		paperAnswerList.forEach(paperAnswer -> {
			paperAnswerMap.put(paperAnswer.getUserId(), paperAnswer);
		});
		List<MockUserVO> list = Lists.newArrayList();
		List<LinkedHashMap<String, Object>> userInfo = getUserInfo(mockUserList,userInfoUrl);
		mockUserList.forEach(mockUser -> {

			List<String> idsList = new ArrayList<String>();
			idsList.add(mockUser.getUserId() + "");

//			HttpEntity<String> request = new HttpEntity<String>(idsList.toString(), headers);
//			Map postForObject = restTemplate.postForObject(userUrl, request, Map.class);
//			List<Map> userList = (List<Map>) postForObject.get("data");
//			String name = (String) userList.get(0).get("name");
//			String nick = (String) userList.get(0).get("nick");
//			String mobile = (String) userList.get(0).get("mobile");
//			Integer uid = Integer.parseInt(String.valueOf(userList.get(0).get("id")));
			Optional<LinkedHashMap<String, Object>> first = userInfo.stream().filter(i -> MapUtils.getInteger(i, "id", -1).equals(mockUser.getUserId()))
					.findFirst();
			if(!first.isPresent()){
				return;
			}
			LinkedHashMap<String, Object> userMap = first.get();
			String name = MapUtils.getString(userMap, "name", "");
			String nick = MapUtils.getString(userMap, "nick", "");
			String mobile = MapUtils.getString(userMap, "mobile", "");
			Integer uid = mockUser.getUserId();
			EssayPaperAnswer essayPaperAnswer = paperAnswerMap.get(uid);
			MockUserVO build = MockUserVO.builder().userName(name).nick(nick).mobile(mobile)
					.enrollTime(mockUser.getGmtCreate()).build();
			if (essayPaperAnswer != null) {
				build.setStartTime(essayPaperAnswer.getCorrectDate());
			}
			list.add(build);

		});
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("申论报名信息");
		HSSFRow row = sheet.createRow((int) 0);
		HSSFCellStyle style = wb.createCellStyle();

		HSSFCell cell = row.createCell((short) 0);
		cell.setCellValue("用户名");
		cell.setCellStyle(style);

		cell = row.createCell((short) 1);
		cell.setCellValue("昵称");
		cell.setCellStyle(style);

		cell = row.createCell((short) 2);
		cell.setCellValue("手机号");
		cell.setCellStyle(style);

		cell = row.createCell((short) 3);
		cell.setCellValue("申论报名时间");
		cell.setCellStyle(style);

		cell = row.createCell((short) 4);
		cell.setCellValue("申论批改时间");
		cell.setCellStyle(style);

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		for (int i = 0; i < list.size(); i++) {
			row = sheet.createRow((int) i + 1);
			MockUserVO userinfo = list.get(i);
			row.createCell((short) 0).setCellValue(userinfo.getUserName());
			row.createCell((short) 1).setCellValue(userinfo.getNick());
			row.createCell((short) 2).setCellValue(userinfo.getMobile());
			String dateString = formatter.format(userinfo.getEnrollTime());
			row.createCell((short) 3).setCellValue(dateString);
			String correctDate = "";
			if (userinfo.getStartTime() != null) {
				correctDate = formatter.format(userinfo.getStartTime());
			}
			row.createCell((short) 4).setCellValue(correctDate);

		}
		try {

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			//File file = new File("/tmp/excel/"+paperId+".xls");
			wb.write(bos);
			byte[] barray = bos.toByteArray();
			InputStream is = new ByteArrayInputStream(barray);
			String dateString = formatter.format(new Date());
			response.setCharacterEncoding("utf-8");
			response.setHeader("Content-Disposition", "attachment;filename=" + dateString+".xls");
			response.setHeader("Content-Type", "application/octet-stream");
//			FileCopyUtils.copy(is,new FileOutputStream(file));
			FileCopyUtils.copy(is, response.getOutputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static List<LinkedHashMap<String, Object>> getUserInfo(List<EssayMockUserMeta> mockUserList,String url){
		int i = 0;
		int size = 100;
		List<LinkedHashMap<String, Object>> data = Lists.newArrayList();
		while (i < mockUserList.size()) {
			int end = (i + size) < mockUserList.size() ? (i + size) : mockUserList.size();
			List<EssayMockUserMeta> tempList = mockUserList.subList(i, end);
			List<UserDto> userDtos = tempList.stream().map(EssayMockUserMeta::getUserId).map(id -> UserDto.builder().id(id).build()).collect(Collectors.toList());
			assertUserInfo(userDtos, data,url);
			i = end;
			//System.out.println("end = " + end);
		}
		return data;
	}

	private static void assertUserInfo(List<UserDto> userDtos, List<LinkedHashMap<String, Object>> data,String userUrl) {
		//String url = "http://123.103.86.52/u/essay/statistics/user";
		//String url = "https://ns.huatu.com/u/essay/statistics/user";

		RestTemplate restTemplate = new RestTemplate();
		ResponseMsg<List<LinkedHashMap<String, Object>>> userDtoList = restTemplate.postForObject(userUrl, userDtos, ResponseMsg.class);
		data.addAll(userDtoList.getData());
	}

}
