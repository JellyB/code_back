package com.huatu.ztk.pc.service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.alibaba.dubbo.common.json.JSON;
import com.alibaba.dubbo.common.json.JSONObject;
import com.baidu.disconf.core.common.utils.GsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.huatu.ztk.commons.AreaConstants;
import com.huatu.ztk.commons.CatgoryType;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.SubjectType;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.knowledge.api.SubjectDubboService;
import com.huatu.ztk.paper.api.PracticeCardDubboService;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.CardUserMeta;
import com.huatu.ztk.paper.bean.Match;
import com.huatu.ztk.paper.bean.MatchCardUserMeta;
import com.huatu.ztk.paper.bean.StandardCard;
import com.huatu.ztk.paper.common.AnswerCardType;
import com.huatu.ztk.paper.common.PaperType;
import com.huatu.ztk.pc.bean.NetSchoolResponse;
import com.huatu.ztk.pc.bean.Share;
import com.huatu.ztk.pc.common.ShareType;
import com.huatu.ztk.pc.dao.MatchDao;
import com.huatu.ztk.pc.dao.ShareDao;
import com.huatu.ztk.pc.util.Crypt3Des;
import com.huatu.ztk.pc.util.UploadFileUtil;
import com.huatu.ztk.pc.util.WxChatShareUtil;
import com.huatu.ztk.report.bean.ModuleSummary;
import com.huatu.ztk.report.bean.PowerSummary;
import com.huatu.ztk.report.bean.PracticeSummary;
import com.huatu.ztk.report.dubbo.ModuleSummaryDubboService;
import com.huatu.ztk.report.dubbo.PowerSummaryDubboService;
import com.huatu.ztk.report.dubbo.PracticeSummaryDubboService;
import com.huatu.ztk.user.bean.UserDto;
import com.huatu.ztk.user.dubbo.UserDubboService;

/**
 * 分享服务层
 * Created by shaojieyue
 * Created time 2016-09-18 17:40
 */

@Service
public class ShareService {
    private static final Logger logger = LoggerFactory.getLogger(ShareService.class);
    public static final String REPORT_SHARE_TITLE = "我在砖题库做题，晒晒我的能力评估报告！";
    public static final String REPORT_SHARE_DESC = "我已使用砖题库做题%s天。通过砖题库的智能评估预算，我参加考试的预测分为%s分";
    ;
    public static final String PRACTICE_SHARE_DESC = "我在砖题库进行了一次行测练习：%s，共答对了%s道题，用时%s分%s秒。";
    public static final String PRACTICE_SHARE_TITLE = "我在砖题库做题，每天进步看得见！";
    public static final String COURSE_SHARE_TITLE = "【砖题库】（课程名称）%s";
    public static final String COURSE_SHARE_DESC = "砖题库直播课程，汇聚名师大咖，为你公考路上保驾护航。";
    public static final String ARENA_SHARE_RECORD_TITLE = "砖题库竞技赛场，真正的公考实力较量！";
    public static final String ARENA_SHARE_RECORD_DESC = "我在砖题库公务员竞技赛场PK，真正的较量才刚刚开始！你敢来吗？";
    public static final String ARENA_SHARE_SUMMARY_TITLE = "砖题库公考对战，来一场真正的较量吧！";
    public static final String ARENA_SHARE_SUMMARY_DESC = "我在砖题库公务员竞技赛场PK，真正的较量才刚刚开始！你敢来吗？";
    public static final String ARENA_SHARE_HTONLINE_TITLE = "华图在线公考对战，来一场真正的较量吧！";
    public static final String ARENA_SHARE_HTONLINE_DESC = "我在华图在线公务员竞技赛场PK，真正的较量才刚刚开始！你敢来吗？";
    public static final String ARENA_SHARE_TODAYRANK_TITLE = "砖题库竞技赛场，真正的公考实力较量！";
    public static final String ARENA_SHARE_TODAYRANK_DESC = "我在砖题库公务员竞技赛场PK，今日排行前三名哦！你敢来挑战吗？";

    public static String COURSE_DESC_URL = "https://apitk.huatu.com/h5/detail_zhuanti_contents.php?isCurl=1&rid=";
    public static String COURSE_BASIC_URL = "https://apitk.huatu.com/Class_Details_Buy.php?p=";
    public static String COURSE_BASIC_URL_V2 = "http://api.huatu.com/lumenapi/v5/c/class/share_class_details?classId=";
//    public static final String COURSE_BASIC_URL = "http://tk.htexam.com/Class_Details_Buy.php?p=";
    /**
     * 课程分享界面立即打开按钮链接
     */
    public static String HANDHELD_HUATU_URL = "https://ns.huatu.com/h5/index.html";

    public static String NS_HUATU_COM = "ns.huatu.com";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    static {
        final String env = System.getProperty("disconf.env");
        if (!env.equalsIgnoreCase("online")) {//说明是测试环境,设置测试环境地址
            NS_HUATU_COM = "123.103.86.52";
            COURSE_BASIC_URL = "http://tk.htexam.com/Class_Details_Buy.php?p=";
            COURSE_BASIC_URL_V2 = "http://testapi.huatu.com/lumenapi/v5/c/class/share_class_details?classId=";
            HANDHELD_HUATU_URL = "http://test-ns.htexam.com/new-h5/h5/index.html";
            COURSE_DESC_URL = "https://tk.htexam.com/h5/detail_zhuanti_contents.php?isCurl=1&rid=";
        }
    }

    @Autowired
    private PracticeCardDubboService practiceCardDubboService;

    @Autowired
    private PracticeSummaryDubboService practiceSummaryDubboService;

    @Autowired
    private UserDubboService userDubboService;

    @Autowired
    private ShareDao shareDao;

    @Autowired
    private PowerSummaryDubboService powerSummaryDubboService;

    @Autowired
    private ModuleSummaryDubboService moduleSummaryDubboService;

//    @Autowired
//    private ArenaPlayerDubboService arenaPlayerDubboService;
//
//    @Autowired
//    private ArenaDubboService arenaDubboService;

//    @Autowired
//    private ArenaUserSummaryDubboService arenaUserSummaryDubboService;

    @Autowired
    private SubjectDubboService subjectDubboService;

    @Autowired
    private MatchDao matchDao;
    @Autowired
    private UploadFileUtil uploadFileUtil;

    /**
     * 分享试题
     *
     * @param qid
     * @return
     */
    public Share shareQuestion(int qid) {
        //公考路上的小伙伴，来道砖题库的题PK下吧！
        //微博、qq分享   显示内容 ->
//        公考路上的小伙伴，来道砖题库的题PK下吧！
//        下载砖题库，听每天免费的公考直播课。
        final Share share = Share.builder()
                .desc("下载砖题库，听每天免费的公考直播课。")
                .title("公考路上的小伙伴，来道砖题库的题PK下吧！")
                .url("http://" + NS_HUATU_COM + "/pc/share/question/" + qid)
                .build();
        return share;
    }


    /**
     * 分享练习结果
     *
     * @param practiceId
     * @param uid
     * @return
     */
    public Share sharePractice(long practiceId, long uid, int catgory) throws BizException {
        final AnswerCard answerCard = practiceCardDubboService.findById(practiceId);
        if (answerCard == null || answerCard.getUserId() != uid) {//只能分享自己的
            throw new BizException(CommonErrors.PERMISSION_DENIED);
        }
        String id = generaChareId();
        final Duration duration = Duration.ofSeconds(answerCard.getExpendTime());
        long minutes = duration.toMinutes(); //75
        final long seconds = duration.minusMinutes(minutes).getSeconds();

        final Share share = Share.builder()
                .title(PRACTICE_SHARE_TITLE)
                .desc(String.format(PRACTICE_SHARE_DESC, answerCard.getName(), answerCard.getRcount(), minutes, seconds))
                .type(ShareType.SHARE_PRACTICE)
                .outerId(practiceId + "," + catgory)
                .url("http://" + NS_HUATU_COM + "/pc/share/practice/" + id)
                .id(id)
                .build();
        shareDao.insert(share);
        return share;
    }

    public Share shareReport(long uid, int subject, int area) {
        //用来获得练习天数
        final PracticeSummary practiceSummary = practiceSummaryDubboService.findByUid(uid, subject);
        //用来获得总分
        final PowerSummary powerSummary = powerSummaryDubboService.find(uid, subject, area);
        String id = generaChareId();
        final Share share = Share.builder()
                .title(REPORT_SHARE_TITLE)
                .desc(String.format(REPORT_SHARE_DESC, practiceSummary.getDayCount(), powerSummary.getScore()))
                .type(ShareType.SHARE_REPORT)
                .outerId(uid + "," + subject + "," + area)
                .url("http://" + NS_HUATU_COM + "/pc/share/myreport/" + id)
                .id(id)
                .build();
        shareDao.insert(share);
        return share;
    }

    /**
     * 组装分享练习model
     *
     * @param id
     * @return
     */
    public Map findSharePracticeModel(String id) throws BizException {
        final Share share = shareDao.findById(id);
        if (share == null) {//分享未找到
            return null;
        }
        String[] outerIds = share.getOuterId().split(",");
        long practiceId = Long.valueOf(outerIds[0]);
        int catgory = Integer.valueOf(outerIds.length >= 2 ? outerIds[1] : CatgoryType.GONG_WU_YUAN + "");
        final AnswerCard answerCard = practiceCardDubboService.findCardTotalInfoById(practiceId);
        if (answerCard == null) {//未查询到对应的答题卡
            return null;
        }
        //试题个数
        final int qcount = answerCard.getAnswers().length;
        final int expendTime = answerCard.getExpendTime();
        final Duration duration = Duration.ofSeconds(expendTime);
        //耗时分钟数
        long minutes = duration.toMinutes(); //75
        //耗时秒数
        final long seconds = duration.minusMinutes(minutes).getSeconds();
        //答对试题数
        final int rcount = answerCard.getRcount();
        Map data = Maps.newHashMap();
        data.put("qcount", qcount);
        data.put("minutes", minutes);
        data.put("seconds", seconds);
        data.put("rcount", rcount);
        data.put("typeName", AnswerCardType.getTypeName(answerCard.getType()));//练习类型
        //兼容 课中练习、课后练习
        if (answerCard.getType() == 15) {
            data.put("typeName", "课后作业");
        } else if (answerCard.getType() == 16) {
            data.put("typeName", "随堂联系");
        }
        data.put("catgory", catgory);
        logger.info("share info = {}", JsonUtil.toJson(data));
        if (answerCard instanceof StandardCard) {
            StandardCard standardCard = (StandardCard) answerCard;
            int type = standardCard.getPaper().getType();

            if (type == PaperType.MATCH) {
                MatchCardUserMeta matchMeta = standardCard.getMatchMeta();
                CardUserMeta cardUserMeta = standardCard.getCardUserMeta();

                if (matchMeta != null && cardUserMeta != null) {
                    data.put("score", standardCard.getScore());
                    data.put("totalRank", cardUserMeta.getRank() + "/" + cardUserMeta.getTotal());
                    data.put("positionRank", matchMeta.getPositionRank() + "/" + matchMeta.getPositionCount());
                    data.put("average", cardUserMeta.getAverage());
                    data.put("matchMark", "mark");
                }
            }
        }
        try {
            String answerCardString = JSON.json(answerCard);
        } catch (IOException e) {
            e.printStackTrace();
        }
        data.put("answerCard", answerCard);
        WxChatShareUtil.assertWeiXinInfo(data, share);
        return data;
    }

    public Map findShareReportModel(String id) {
        final Share share = shareDao.findById(id);
        if (share == null) {//分享未找到
            return null;
        }
        String outerId = share.getOuterId();
        if (StringUtils.isEmpty(outerId)) {
            return null;
        }
        String[] outerIds = outerId.split(",");

        long userId = Long.valueOf(outerIds[0]);
        int subject = Integer.valueOf(outerIds.length >= 2 ? outerIds[1] : SubjectType.GWY_XINGCE + "");
        int area = Integer.valueOf(outerIds.length >= 3 ? outerIds[2] : AreaConstants.QUAN_GUO_ID + "");

        final List<ModuleSummary> moduleSummaries = moduleSummaryDubboService.find(userId, subject);
        for (ModuleSummary moduleSummary : moduleSummaries) {
            switch (moduleSummary.getModuleId()) {
                case 392:
                    moduleSummary.setModuleName("常识");
                    break;
                case 435:
                    moduleSummary.setModuleName("言语");
                    break;
                case 482:
                    moduleSummary.setModuleName("数量");
                    break;
                case 642:
                    moduleSummary.setModuleName("判断");
                    break;
                case 754:
                    moduleSummary.setModuleName("资料");
                    break;
                case 3125:
                    moduleSummary.setModuleName("政治");
                    break;
                case 3195:
                    moduleSummary.setModuleName("经济");
                    break;
                case 3250:
                    moduleSummary.setModuleName("管理");
                    break;
                case 3280:
                    moduleSummary.setModuleName("公文");
                    break;
                case 3298:
                    moduleSummary.setModuleName("人科");
                    break;
                case 3332:
                    moduleSummary.setModuleName("法律");
                    break;
            }
        }

        final PowerSummary powerSummary = powerSummaryDubboService.find(userId, subject, area);
        Map data = Maps.newHashMap();
        data.put("moduleSummaries", moduleSummaries);
        data.put("powerSummary", powerSummary);
        int catgory = subjectDubboService.getCatgoryBySubject(subject);
        logger.info("练习报告分享，{}", catgory);
        data.put("catgory", catgory);
        WxChatShareUtil.assertWeiXinInfo(data, share);
        return data;
    }

    /**
     * 生成课程分享对象
     *
     * @param courseId
     * @return
     */
    public Share shareCourse(long courseId, long uid, int catgory) {
        String id = generaChareId();
        final Share share = Share.builder()
                .title(String.format(COURSE_SHARE_TITLE, getCourseName(courseId, uid)))
                .desc(COURSE_SHARE_DESC)
                .type(ShareType.SHARE_COURSE)
                .outerId(courseId + "," + uid + "," + catgory)
                .url("http://" + NS_HUATU_COM + "/pc/share/course/" + id)
                .id(id)
                .build();
        shareDao.insert(share);
        return share;
    }

    private String generaChareId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 获取课程名称
     *
     * @param courseId
     * @param uid
     * @return
     */
    public String getCourseName(long courseId, long uid) {
        logger.info("courseid={}", courseId);
        StopWatch stopWatch = new StopWatch("getCourseName,"+courseId+","+uid);
        //获取课程基本信息
        try {
            stopWatch.start("getCourseBasicData");
            String courseBasic = getCourseBasicData(courseId, uid);
            stopWatch.stop();
            if (StringUtils.isNotEmpty(courseBasic)) {
                stopWatch.start("convertStr");
                ObjectMapper mapper = new ObjectMapper();
                JsonNode contentNode = mapper.readTree(courseBasic);
                final JsonNode techInfos = contentNode.get("teacher_informatioin");
                String courseName = techInfos.get("Title").asText();    //课程名称
                stopWatch.stop();
                return convertStr(courseName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            logger.info(stopWatch.prettyPrint());
        }
        return "";
    }
    
    /**
     * 获取课程基本信息v2
     * @param courseId
     * @return
     */
	public String getCourseNameV2(long courseId) {
		logger.info("getCourseNameV2 courseid={}", courseId);
		// 获取课程基本信息
		try {
			Map courseBasicMap = getCourseBasicDataV2(courseId);
			String courseName = courseBasicMap.get("title").toString(); // 课程名称
			return convertStr(courseName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

    /**
     * 组装分享的课程
     *
     * @param shareId
     * @return
     */
    public Map findShareCourse(String shareId) {
        Share share = shareDao.findById(shareId);
        if (share == null) { //分享的课程未找到
            return null;
        }
        try {
            //课程id [0],用户id[1]
            String[] outerIds = share.getOuterId().split(",");
            //获取课程基本信息
            //String resultCourse = getCourseBasicData(Long.parseLong(outerIds[0]), Long.parseLong(outerIds[1]));
            Map resultCourseMap = getCourseBasicDataV2(Long.parseLong(outerIds[0]));
            logger.info("resultCourse={}", resultCourseMap);
            Map<String, Object> courseMap = new HashMap<>();
            WxChatShareUtil.assertWeiXinInfo(courseMap, share);
			if (resultCourseMap != null) {
				// ObjectMapper mapper = new ObjectMapper();
				// JsonNode contentNode = mapper.readTree(resultCourse);
				// final JsonNode techInfos = contentNode.get("teacher_informatioin");
				// logger.info("resultCourse:{}", resultCourse);
				String courseId = resultCourseMap.get("rid").toString(); // 课程id
				String courseName = resultCourseMap.get("title").toString(); // 课程名称
				String starttime = resultCourseMap.get("startTime").toString(); // 课程开始时间
				String timesLength = resultCourseMap.get("timeLength").toString(); // 课时
				String teacherNames = resultCourseMap.get("teacherName").toString(); // 教师
				String studyDate = resultCourseMap.get("studyDate").toString();// 直播时间
				String scaleImg = resultCourseMap.get("scaleImg").toString();// 缩略图
				// 处理教师头像
				ArrayList<String> roundPhoto = new ArrayList<>();
				if (null != resultCourseMap.get("teacherImg")
						&& StringUtils.isNotBlank(resultCourseMap.get("teacherImg").toString())) {
					String[] photos = resultCourseMap.get("teacherImg").toString().split(",");
					roundPhoto.addAll(Arrays.asList(photos));
				} else {
					IntStream.range(0, convertStr(teacherNames).split(",").length + 1)
							.forEach(index -> roundPhoto.add("http://v.huatu.com/images/default_teacher.jpg"));

				}
				// courseMap.put("imgUrl", uploadFileUtil.moveImgToTiku(scaleImg)); //覆盖分享自定义的图片
				courseMap.put("imgUrl", scaleImg);
				courseMap.put("courseid", courseId);
				courseMap.put("courseName", convertStr(courseName));
				courseMap.put("starttime", convertStr(starttime));
				courseMap.put("timesLength", convertStr(timesLength));
				courseMap.put("studyDate", convertStr(studyDate));
				courseMap.put("teacherNames", convertStr(teacherNames));
				courseMap.put("teacherRoundPhoto", roundPhoto);
			} else {
                return null;
            }
            Stopwatch stopwatch = Stopwatch.createStarted();
            //获取课程介绍
            String courseDescUrl = COURSE_DESC_URL + outerIds[0];
            String courseDesc = getHttpResponseData(courseDescUrl);
            logger.info("zhouwei2:" + stopwatch.elapsed(TimeUnit.MILLISECONDS));
            courseMap.put("courseDesc", convertStr(courseDesc));
            courseMap.put("courseDescUrl", convertStr(courseDescUrl));
            courseMap.put("catgory", Integer.parseInt(outerIds.length == 3 ? outerIds[2] : "1"));
            return courseMap;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取课程基本信息
     *
     * @param courseId
     * @param uid
     * @return
     */
    private String getCourseBasicData(long courseId, long uid) throws BizException, IOException {
        //获取用户x信息
        StopWatch stopWatch = new StopWatch("getCourseBasicData,"+courseId+","+uid);
        stopWatch.start("user findById:" + uid);
        final UserDto userDto = userDubboService.findById(uid);
        stopWatch.stop();
        stopWatch.start("encryptMode");
        logger.info("rid={},uid={},uname={}", courseId, uid, userDto.getName());
        String plaintext = "rid=" + courseId + "&username=" + userDto.getName();
        //获取课程基本信息url
        String courseBaseUrl = COURSE_BASIC_URL + Crypt3Des.encryptMode(plaintext);
        stopWatch.stop();
        stopWatch.start("getHttpResponseData");
        logger.info("courseBaseUrl={}", courseBaseUrl);
        //获取课程基本信息
        String result = getHttpResponseData(courseBaseUrl);
        stopWatch.stop();
        stopWatch.start("getCourseObject");
        logger.info("result={}", result);
        //获取解析的课程结果
        String courseObject = getCourseObject(result,true);
        stopWatch.stop();
        logger.info(stopWatch.prettyPrint());
        return courseObject;
    }

    /**
     * 获取课程基本信息v2
     * @param courseId
     * @return
     * @throws BizException
     */
	private Map getCourseBasicDataV2(long courseId) throws BizException {
		// 获取课程基本信息url
		String courseBaseUrl = COURSE_BASIC_URL_V2 + courseId;
		logger.info("getCourseBasicDataV2 courseBaseUrl={}", courseBaseUrl);
		// 获取课程基本信息
		String result = getHttpResponseData(courseBaseUrl);
		logger.info("getCourseBasicDataV2 result={}", result);
		// 获取解析的课程结果
		Map courseMap = getCourseMap(result);
		return courseMap;
	}

    

    /**
     * 解析课程结果
     *
     * @param result
     * @return
     * @throws BizException
     */
    private String getCourseObject(String result, boolean decrypt) throws BizException {
        StopWatch stopwatch = new StopWatch("getCourseObject");
        try{
            if (StringUtils.isEmpty(result)) {
                return null;
            }
            stopwatch.start("JsonConvert");
            NetSchoolResponse netSchoolResponse = JsonUtil.toObject(result, NetSchoolResponse.class);
            stopwatch.stop();
            if (netSchoolResponse == null) {
                return null;
            }
            if (netSchoolResponse.getCode() != 1 && netSchoolResponse.getCode() != 10000) {  //1:表示成功 10000新接口成功状态
                throw new BizException(ErrorResult.create(netSchoolResponse.getCode(), netSchoolResponse.getMsg()));
            }
            //只取data部分
            stopwatch.start("getData");
            Object data = netSchoolResponse.getData();
            stopwatch.stop();
            if (data == null) {  //code为1，但是没有data
                return null;
            }
            stopwatch.start("decrytpMode");
            if(decrypt) {
            	String s = Crypt3Des.decrytpMode(String.valueOf(data));
            	stopwatch.stop();
            	return s;
            }else {
            	return String.valueOf(data);
            }
        }finally {
            logger.info(stopwatch.prettyPrint());
        }
    }
    
	private Map getCourseMap(String result) throws BizException {
		if (StringUtils.isEmpty(result)) {
			return null;
		}
		NetSchoolResponse netSchoolResponse = JsonUtil.toObject(result, NetSchoolResponse.class);
		if (netSchoolResponse == null) {
			return null;
		}
		if (netSchoolResponse.getCode() != 1 && netSchoolResponse.getCode() != 10000) { // 1:表示成功 10000新接口成功状态
			throw new BizException(ErrorResult.create(netSchoolResponse.getCode(), netSchoolResponse.getMsg()));
		}
		// 只取data部分
		Map data = (Map) netSchoolResponse.getData();
		if (data == null) { // code为1，但是没有data
			return null;
		}
		return data;
	}

    /**
     * 获取http请求的返回数据
     *
     * @param url
     * @return
     */
    private String getHttpResponseData(String url) {
        StopWatch stopWatch = new StopWatch("getHttpResponseData:" + url);
        try {
            stopWatch.start("opsForValue");
            String value = redisTemplate.opsForValue().get(url);
            stopWatch.stop();
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
            CloseableHttpClient client = null;
            CloseableHttpResponse response = null;
            String result = "";
            Exception exception = null;
            try {
                stopWatch.start("httpGet0");
                client = HttpClients.createDefault();
                stopWatch.stop();
                stopWatch.start("httpGet1");
                HttpGet e = new HttpGet(url);
                stopWatch.stop();
                stopWatch.start("httpGet2");
                RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(3000).setConnectTimeout(3000).build();
                stopWatch.stop();
                stopWatch.start("httpGet3");
                e.setConfig(requestConfig);
                stopWatch.stop();
                stopWatch.start("httpGet4");
                response = client.execute(e);
                stopWatch.stop();
                stopWatch.start("getEntity");
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    result = EntityUtils.toString(resEntity, Charset.forName("utf-8"));
                }
                stopWatch.stop();
            } catch (Exception e) {
                logger.warn("request fail,url={}", url, e);
            } finally {
                stopWatch.start("response close");
                if (response != null) {
                    try {
                        response.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                if (client != null) {
                    try {
                        client.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            redisTemplate.opsForValue().set(url, result);
            redisTemplate.expire(url, 1, TimeUnit.DAYS);
            stopWatch.stop();
            return result;
        }finally {
            logger.info(stopWatch.prettyPrint());
        }

    }

    /**
     * 分享竞技成绩统计
     *
     * @param uid
     * @param roomid
     * @return
     */
    public Share shareArenaRecord(long uid, long roomid) throws BizException {
//        ArenaRoom arenaRoom=arenaDubboService.findById(roomid);
//        if (arenaRoom == null) {
//            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
//        }
//        if(!arenaRoom.getPlayerIds().contains( uid)){//只能分享自己的
//            throw new BizException(CommonErrors.PERMISSION_DENIED);
//        }
        String shareid = generaChareId();
        final Share share = Share.builder()
                .title(ARENA_SHARE_RECORD_TITLE)
                .desc(ARENA_SHARE_RECORD_DESC)
                .type(ShareType.SHARE_ARENA_RECORD)
                .outerId(roomid + "," + uid)
                .url("http://" + NS_HUATU_COM + "/pc/share/arena/record/" + shareid)
                .id(shareid)
                .build();
        shareDao.insert(share);
        return share;
    }

    /**
     * 分享竞技成绩统计的内容
     *
     * @param id
     * @return
     */
    public Map findArenaRecord(String id) {
        Share share = shareDao.findById(id);
//        if(share==null){ //分享的竞技成绩统计未找到
//            return null;
//        }
//        //竞技房间id [0],用户id[1]
//        String[] outerIds=share.getOuterId().split(",");
//       ArenaRoom arenaRoom=arenaDubboService.findById(Long.parseLong(outerIds[0]));
//        if(arenaRoom==null){ //未找到竞技记录
//            return  null;
//        }
//        Player player=arenaPlayerDubboService.findById(Long.parseLong(outerIds[1]));
//        if (player==null){
//            return  null;
//        }
//        //该用户是否竞技成功0：失败，1：成功
//        int isWin=0;
//        if(arenaRoom.getWinner()==Long.parseLong(outerIds[1])){
//            isWin=1;
//        }
//        Map areaRecordMap=Maps.newHashMap();
//        WxChatShareUtil.assertWeiXinInfo(areaRecordMap,share);
//        areaRecordMap.put("uid",player.getUid());
//        areaRecordMap.put("nick",player.getNick());
//        areaRecordMap.put("avatar",player.getAvatar());
//        areaRecordMap.put("typeName",arenaRoom.getModule());
//        areaRecordMap.put("createtime",arenaRoom.getCreateTime());
//        areaRecordMap.put("isWin",isWin);
        return Maps.newHashMap();
    }

    /**
     * 竞技练习战绩分享
     *
     * @param uid
     * @return
     */
    public Share shareArenaSummary(long uid) {
        String shareid = generaChareId();
        final Share share = Share.builder()
                .title(ARENA_SHARE_SUMMARY_TITLE)
                .desc(ARENA_SHARE_SUMMARY_DESC)
                .type(ShareType.SHARE_ARENA_SUMMARY)
                .outerId(uid + "")
                .url("http://" + NS_HUATU_COM + "/pc/share/arena/summary/" + shareid)
                .id(shareid)
                .build();
        shareDao.insert(share);
        return share;
    }

    /**
     * 竞技练习战绩分享的内容的组装
     *
     * @param id
     * @return
     */
    public Map findArenaSummary(String id) {
        final Share share = shareDao.findById(id);
        if (share == null) { //分享的的战绩未找到
            return null;
        }
        Long uid = Long.parseLong(share.getOuterId());
        //用户信息
//       Player player= arenaPlayerDubboService.findById(uid);
//       final ArenaUserSummary arenaUserSummary= arenaUserSummaryDubboService.findSummaryById(uid);
//        int winCount=0;  //竞技胜的次数的计数器
//        int failCount=0;//竞技失败的次数的计数器
//        int totalCount=1;//竞技总次数  避免分母为0
//        if(arenaUserSummary!=null){  //竞技次数为0
//            winCount=arenaUserSummary.getWinCount();
//            failCount=arenaUserSummary.getFailCount();
//            totalCount=winCount+failCount;
//        }
//
//        Map data = Maps.newHashMap();
//        WxChatShareUtil.assertWeiXinInfo(data,share);
//        data.put("nick",player.getNick());  //昵称
//        data.put("avatar",player.getAvatar()); //头像
//        data.put("winCount",winCount); //竞技胜的次数
//        data.put("failCount",failCount); //竞技失败的次数
//        data.put("winPercent", NumberFormatUtil.getPercent((double) winCount/totalCount,2)); //竞技胜的百分比
//        data.put("failPercent",NumberFormatUtil.getPercent(1-(double) winCount/totalCount,2)); //竞技失败的百分比
//        return data;
        return Maps.newHashMap();
    }

    /**
     * 华图在线分享
     *
     * @param uid
     * @return
     */
    public Share shareArenaSummaryOnline(long uid) {
        String shareid = generaChareId();
        final Share share = Share.builder()
                .title(ARENA_SHARE_HTONLINE_TITLE)
                .desc(ARENA_SHARE_HTONLINE_DESC)
                .type(ShareType.SHARE_ARENA_SUMMARY)
                .outerId(uid + "")
                .url("http://" + NS_HUATU_COM + "/pc/share/arena/htonline/" + shareid)
                .id(shareid)
                .build();
        shareDao.insert(share);
        return share;
    }

    /**
     * 竞技今日排行
     *
     * @param uid
     * @return
     */
    public Share shareArenaTodayRank(long uid) {
        String shareid = generaChareId();
        final Share share = Share.builder()
                .title(ARENA_SHARE_TODAYRANK_TITLE)
                .desc(ARENA_SHARE_TODAYRANK_DESC)
                .type(ShareType.SHARE_ARENA_TODAYRANK)
                .outerId(uid + "")
                .url("http://" + NS_HUATU_COM + "/pc/share/arena/todayrank/" + shareid)
                .id(shareid)
                .build();
        shareDao.insert(share);
        return share;
    }

    /**
     * 竞技今日排行的组装
     *
     * @param id
     * @return
     */
    public Map findArenaTodayRank(String id) {
        Share share = shareDao.findById(id);
        if (share == null) {//分享的的今日排行未找到
            return null;
        }
        //用户信息
        //Player player= arenaPlayerDubboService.findById(Long.parseLong(share.getOuterId()));
        Map data = Maps.newHashMap();
        WxChatShareUtil.assertWeiXinInfo(data, share);
        data.put("nick", "nick");  //昵称
        data.put("avatar", "avatar"); //头像
        return data;
    }

    /**
     * 字符转换
     *
     * @param value
     * @return
     */
    private static String convertStr(String value) {
        if (StringUtils.isEmpty(value) || value.toUpperCase().equals("NULL")) {
            return "";
        }
        return value;
    }

    public Match findMatchById(int paperId) {
        return matchDao.findById(paperId);
    }

    public Share shareMatch(int paperId) {
        return Share.builder()
                .title("模考大赛")
                .url("http://" + NS_HUATU_COM + "/pc/share/match/" + paperId)
                .desc("模考大赛描述")
                .build();
    }

    /**
     * 组装分享练习model
     *
     * @param id
     * @return
     */
    public Map findSharePracticeModelWithEssay(String id) throws BizException {
        final Share share = shareDao.findById(id);
        if (share == null) {//分享未找到
            return null;
        }
        Map result = Maps.newHashMap();
        WxChatShareUtil.assertWeiXinInfo(result, share);
        String[] outerIds = share.getOuterId().split(",");
        int reportType = (outerIds.length >= 3) ? Integer.parseInt(outerIds[2]) : 0;
        if (reportType == 0 || reportType == 1) {
            result.putAll(getCommonShareModel(outerIds));
        } else {
            result.putAll(share.getReportInfo());
        }
        return result;
    }

    /**
     * 普通的报告模式
     *
     * @param outerIds
     * @return
     * @throws BizException
     */
    public Map getCommonShareModel(String[] outerIds) throws BizException {
        long practiceId = Long.valueOf(outerIds[0]);
        int catgory = Integer.valueOf(outerIds.length >= 2 ? outerIds[1] : CatgoryType.GONG_WU_YUAN + "");
        final AnswerCard answerCard = practiceCardDubboService.findCardTotalInfoById(practiceId);
        if (answerCard == null) {//未查询到对应的答题卡
            return null;
        }
        //试题个数
        final int qcount = answerCard.getAnswers().length;
        final int expendTime = answerCard.getExpendTime();
        final Duration duration = Duration.ofSeconds(expendTime);
        //耗时分钟数
        long minutes = duration.toMinutes(); //75
        //耗时秒数
        final long seconds = duration.minusMinutes(minutes).getSeconds();
        //答对试题数
        final int rcount = answerCard.getRcount();
        Map data = Maps.newHashMap();
        data.put("qcount", qcount);
        data.put("minutes", minutes);
        data.put("seconds", seconds);
        data.put("rcount", rcount);
        data.put("typeName", AnswerCardType.getTypeName(answerCard.getType()));//练习类型
        data.put("catgory", catgory);
        logger.info("share info = {}", JsonUtil.toJson(data));
        if (answerCard instanceof StandardCard) {
            StandardCard standardCard = (StandardCard) answerCard;
            int type = standardCard.getPaper().getType();

            if (type == PaperType.MATCH) {
                MatchCardUserMeta matchMeta = standardCard.getMatchMeta();
                CardUserMeta cardUserMeta = standardCard.getCardUserMeta();

                if (matchMeta != null && cardUserMeta != null) {
                    data.put("score", standardCard.getScore());
                    data.put("totalRank", cardUserMeta.getRank() + "/" + cardUserMeta.getTotal());
                    data.put("positionRank", matchMeta.getPositionRank() + "/" + matchMeta.getPositionCount());
                    data.put("average", cardUserMeta.getAverage());
                    data.put("matchMark", "mark");
                }
            }
        }

        return data;
    }

    public String getUrlForCourseDetail(Map data) {
        StringBuilder sb = new StringBuilder(HANDHELD_HUATU_URL);
        sb.append("?rid=").append(MapUtils.getString(data,"courseid"));
        sb.append("&type=").append(ShareType.SHARE_COURSE);

        return sb.toString();
    }
}
