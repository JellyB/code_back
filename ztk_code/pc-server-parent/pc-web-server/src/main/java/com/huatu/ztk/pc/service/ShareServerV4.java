package com.huatu.ztk.pc.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.paper.api.PracticeCardDubboService;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.common.AnswerCardType;
import com.huatu.ztk.pc.bean.Share;
import com.huatu.ztk.pc.common.ResponseMsg;
import com.huatu.ztk.pc.common.ShareType;
import com.huatu.ztk.pc.constants.ShareReportType;
import com.huatu.ztk.pc.dao.ShareDao;
import com.huatu.ztk.pc.util.RestTemplateUtil;
import com.huatu.ztk.pc.util.WxChatShareUtil;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.huatu.ztk.pc.service.HuatuShareService.*;

/**
 * @author jbzm
 * @date 2018下午5:15
 **/
@Service
public class ShareServerV4 {
    private static final Logger logger = LoggerFactory.getLogger(ShareServerV4.class);
    @Autowired
    private ShareDao shareDao;
    @Autowired
    private PracticeCardDubboService practiceCardDubboService;
    @Autowired
    RedisTemplate<String, String> redisTemplate;

    /**
     * 音频分享查询接口
     */
    private static String SHARE_MUSIC_BASIC_URL = "https://api.huatu.com/lumenapi/v5/c/class/share_music?lessonId=%s&type=5";

    static {
        final String env = System.getProperty("disconf.env");
        //说明是测试环境,设置测试环境地址
        if (!env.equalsIgnoreCase("online")) {
        	SHARE_MUSIC_BASIC_URL = "https://testapi.huatu.com/lumenapi/v5/c/class/share_music?lessonId=%s&type=5";
        }
    }

    @Autowired
    ShareService shareService;

    /**
     * 根据type分发任务
     *
     * @param paperId
     * @param type
     * @param token
     * @param catgory
     * @param cv
     * @param terminal @return
     * @throws BizException
     */
    public Share sharePracticeWithEssayV4(long paperId, int type, String token, int catgory, String cv, int terminal) throws BizException {
        new Share();
        Share share;
        String id = generaChareId();
        switch (type) {
            case 1: {
                share = getShareReportOnlyLineTestV4(paperId, token, catgory);
                break;
            }
            case 2: {
                share = getShareReportOnlyEssayV4(paperId, token, catgory, cv, terminal);
                break;
            }
            case 3: {
                share = getShareReportLineTestWithEssayV4(paperId, token, catgory, cv, terminal);
                break;
            }
            default:
                throw new BizException(CommonErrors.INVALID_ARGUMENTS);
        }
        share.setUrl("http://" + NS_HUATU_COM + "/pc/v4/share/match/" + id);
        share.setId(id);
        logger.info("sharePracticeWithEssayV4:share={}", share);
        shareDao.insert(share);
        return share;
    }

    /**
     * 组装分享练习model
     *
     * @param id
     * @return
     */
    public Map findSharePracticeModelWithEssay(String id) throws BizException {
        final Share share = shareDao.findById(id);
        //分享未找到
        if (share == null) {
            return null;
        }
        String[] outerIds = share.getOuterId().split(",");
        Map result = share.getReportInfo();
        WxChatShareUtil.assertWeiXinInfo(result, share);
        return result;

    }

    /**
     * 获取申论和模考的统计数据
     *
     * @param paperId
     * @param token
     * @param catgory
     * @param cv
     * @param terminal @return
     * @throws BizException
     */
    private Share getShareReportLineTestWithEssayV4(long paperId, String token, int catgory, String cv, int terminal) throws BizException {
        ResponseEntity<ResponseMsg> resultAll = RestTemplateUtil.getTotalReportRequest(paperId, token);
        Map resultMapAll = (Map) resultAll.getBody().getData();
        ResponseEntity<ResponseMsg> resultEssay = RestTemplateUtil.getEessayReporterRequest(Long.valueOf(String.valueOf(resultMapAll.get("essayPaperId"))), token, cv, terminal + "");
        Map resultMapEssay = (Map) resultEssay.getBody().getData();
        ResponseEntity<ResponseMsg> resultMatch = RestTemplateUtil.getMatchReportRequest(paperId, token, 2);
        Map resultMapMatch = (Map) resultMatch.getBody().getData();
        Map<String, Object> allMap = Maps.newHashMap();
        allMap.put("matchMark", "total");
        allMap.put("all", resultMapAll);
        allMap.put("resultMapEssay", resultMapEssay);
        allMap.put("resultMapMatch", resultMapMatch);
        return Share.builder()
                .title(MATCH_SHARE_TITLE)
                .desc(String.format(MATCH_SHARE_TOTAL, resultMapAll.get("score"), resultMapAll.get("lineTestScore"), resultMapAll.get("essayScore")))
                .type(ShareType.SHARE_PRACTICE)
                .outerId(paperId + "," + catgory + "," + ShareReportType.LINETESTWITHESSAY)
                .reportInfo(allMap)
                .build();
    }

    /**
     * 获取申论的统计数据
     *
     * @param paperId
     * @param token
     * @param catgory
     * @param cv
     * @param terminal
     * @return
     * @throws BizException
     */
    private Share getShareReportOnlyEssayV4(long paperId, String token, int catgory, String cv, int terminal) throws BizException {
        try {

            Map<String, Object> result = RestTemplateUtil.getEssayReportByClient(paperId, token, cv, terminal + "");
            Map resultMap = (Map) result;
            resultMap.put("matchMark", "essay");
            Share share = Share.builder()
                    .title(MATCH_SHARE_TITLE)
                    .desc(String.format(MATCH_SHARE_ESSAY, resultMap.get("score"), resultMap.get("minutes"), resultMap.get("seconds")))
                    .type(ShareType.SHARE_PRACTICE)
                    .outerId(paperId + "," + catgory + "," + ShareReportType.ESSAYONLY)
                    .reportInfo(resultMap)
                    .build();
            return share;
        } catch (Exception e) {
            logger.error("查询模考申论信息报告出错：" + e.getMessage());
            e.printStackTrace();
            throw new BizException(CommonErrors.SERVICE_INTERNAL_ERROR);
        }
    }

    /**
     * 获取行测的统计数据
     *
     * @param paperId
     * @param token
     * @param catgory
     * @return
     */
    private Share getShareReportOnlyLineTestV4(long paperId, String token, int catgory) {
        ResponseEntity<ResponseMsg> result = RestTemplateUtil.getMatchReportRequest(paperId, token, 2);
        Map resultMap = (Map) result.getBody().getData();
        final Duration duration = Duration.ofSeconds(Long.valueOf(String.valueOf(resultMap.get("expendTime"))));
        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).getSeconds();
        resultMap.put("matchMark", "mark");
        Share share = Share.builder()
                .title(MATCH_SHARE_TITLE)
                .desc(String.format(MATCH_SHARE_LINE_TEST, resultMap.get("rcount"), minutes, seconds))
                .type(ShareType.SHARE_PRACTICE)
                .outerId(resultMap.get("id") + "," + catgory + "," + ShareReportType.LINETESTONLY)
                .reportInfo(resultMap)
                .build();
        return share;
    }

    /**
     * 生成随机的id
     *
     * @return
     */
    private String generaChareId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public Share sharePractice(long practiceId, String token, String title, int type) throws BizException {
        ResponseEntity<ResponseMsg> result = RestTemplateUtil.getMatchReportRequest(practiceId, token, 1);
        Map data = (Map) result.getBody().getData();
        final AnswerCard answerCard = practiceCardDubboService.findCardTotalInfoById(practiceId);
        if (null == answerCard) {
            logger.info("error  =>>>,{},{},{},{}", practiceId, token, title, type);
            return null;
        }
        final Duration duration = Duration.ofSeconds(answerCard.getExpendTime());
        long minutes = duration.toMinutes();
        final long seconds = duration.minusMinutes(minutes).getSeconds();
        data.put("title", "练习类型：" + AnswerCardType.getTypeName(answerCard.getType()));
        Share share;
        String id = generaChareId();
        share = Share.builder()
                .title(PRACTICE_SHARE_TITLE)
                .id(id)
                .desc(String.format(MATCH_SHARE_DESC, AnswerCardType.getTypeName(answerCard.getType()), answerCard.getRcount(), minutes, seconds))
                .reportInfo(data)
                .url("http://" + NS_HUATU_COM + "/pc/v4/share/practice/" + id)
                .type(type)
                .build();
        shareDao.insert(share);
        return share;
    }

    public Share findSharePractice(String id) {
        final Share share = shareDao.findById(id);
        return share;
    }


    public Share shareCourseOrVideo(long courseId, Long uid, long shareSyllabusId, long courseWareId, int type, int terminal, String cv) throws BizException {
        logger.info("shareCourseOrVideo params = {},{},{},{},{},{},{}", courseId, uid, shareSyllabusId, courseWareId, type, terminal, cv);
        Share share = null;
        String id = generaChareId();
        if (type == 5) {      //音频课件
            if (shareSyllabusId == -1 || courseId == -1 || courseWareId == -1) {
                throw new BizException(CommonErrors.INVALID_ARGUMENTS);
            }
            Map<String, Object> videoBasicData = getVideoBasicData(courseWareId);
            share = Share.builder()
                    .title(String.format(COURSE_SHARE_TITLE, MapUtils.getString(videoBasicData, "title", "")))
                    .desc("公考速记口诀，汇聚名师智慧，为公考学习保驾护航。")
                    .type(ShareType.SHARE_COURSE)
                    .outerId(courseId + "," + uid + "," + shareSyllabusId + "," + courseWareId + "," + terminal + "," + cv)
                    .url(String.format(M_VIDEO_SHARE_URL, courseId, shareSyllabusId, terminal, cv))
                    .id(id)
                    .videoInfo(videoBasicData)
                    .build();
        } else {
            share = Share.builder()
                    .title(String.format(COURSE_SHARE_TITLE, shareService.getCourseName(courseId, uid)))
                    .desc(COURSE_SHARE_DESC)
                    .type(ShareType.SHARE_COURSE)
                    .outerId(courseId + "," + uid + "," + -1)
                    .url(String.format(M_NS_HUATU_COM, courseId))
                    .id(id)
                    .build();
        }

        shareDao.insert(share);
        return share;
    }


    /**
     * 获取音频基本信息
     *
     * @param lessonId
     * @return
     */
    private Map<String, Object> getVideoBasicData(long lessonId) {

        logger.info("getVideoBasicData lessonId={}", lessonId);
        //获取课程基本信息url
        String courseBaseUrl = String.format(SHARE_MUSIC_BASIC_URL, lessonId);
        logger.info("courseBaseUrl={}", courseBaseUrl);
        try {
            //获取课程基本信息
            String result = getHttpResponseData(courseBaseUrl);
            logger.info("result={}", result);
            //获取解析的课程结果
            if (StringUtils.isNotEmpty(result)) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode contentNode = mapper.readTree(result);
                String s = contentNode.toString();
                System.out.println("s = " + s);
                JsonNode data = contentNode.get("data");
                String title = data.get("title").asText("");
                String playUrl = data.get("playUrl").asText("");
                String img = data.get("img").asText("");
                HashMap<String, Object> map = Maps.newHashMap();
                map.put("title", title);
                map.put("videoUrl", playUrl);
                map.put("thumbnail", img);
                return map;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Maps.newHashMap();
    }

    /**
     * 获取http请求的返回数据
     *
     * @param url
     * @return
     */
    private String getHttpResponseData(String url) {
        String value = redisTemplate.opsForValue().get(url);
        if (StringUtils.isNotBlank(value)) {
            return value;
        }
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;
        String result = "";
        Exception exception = null;
        try {
            client = HttpClients.createDefault();
            HttpGet e = new HttpGet(url);
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(3000).setConnectTimeout(3000).build();
            e.setConfig(requestConfig);
            response = client.execute(e);
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                result = EntityUtils.toString(resEntity, Charset.forName("utf-8"));
            }
        } catch (Exception e) {
            logger.warn("request fail,url={}", url, e);
        } finally {
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
        return result;
    }

    /**
     * 
     * @param uid
     * @param practiceId
     * @param avatar
     * @param nick
     * @return
     * @throws BizException 
     */
	public Share estimateForWechat(Long uid, String practiceId, String avatar, String nick) throws BizException {

		final AnswerCard answerCard = practiceCardDubboService.findById(Long.parseLong(practiceId));
		if (answerCard == null || answerCard.getUserId() != uid) {
			throw new BizException(CommonErrors.INVALID_ARGUMENTS);
		}
		Map<String, Object> wechat = Maps.newHashMap();
		wechat.put("avatar", avatar);
		wechat.put("nick", nick);
		wechat.put("answerCardId", answerCard.getId());
		wechat.put("uId", uid);
		String id = generaChareId();
		Share share = Share.builder().desc("公考速记口诀，汇聚名师智慧，为公考学习保驾护航。").type(ShareType.SHARE_REPORT)
				.outerId(uid + "," + practiceId + "," + avatar + "," + nick).id(id).wechatInfo(wechat).build();
		shareDao.insert(share);
		return share;
	}

	public Object getEstimateForWechat(String id, int terminal) throws BizException {
		Map<String, Object> ret = Maps.newHashMap();
		Share share = shareDao.findById(id);
		if (share == null) {
			throw new BizException(CommonErrors.INVALID_ARGUMENTS);
		}
		Map<String, Object> wechatInfo = share.getWechatInfo();
		long start = System.currentTimeMillis();
		AnswerCard answerCard = practiceCardDubboService.findAnswerCardDetail(
				Long.parseLong(wechatInfo.get("answerCardId").toString()),
				Long.parseLong(wechatInfo.get("uId").toString()), terminal, "7.0");
		long end = System.currentTimeMillis();
		logger.info("findAnswerCardDetail spend time:{}", end - start);
		ret.put("share", share);
		ret.put("answerCard", answerCard);
		return ret;
	}

}
