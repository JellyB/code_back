package com.huatu.ztk.pc.service;


import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.paper.api.PracticeCardDubboService;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.Match;
import com.huatu.ztk.paper.bean.MatchUserMeta;
import com.huatu.ztk.paper.common.AnswerCardType;
import com.huatu.ztk.pc.bean.Share;
import com.huatu.ztk.pc.common.ShareType;
import com.huatu.ztk.pc.constants.ShareReportType;
import com.huatu.ztk.pc.dao.MatchDao;
import com.huatu.ztk.pc.dao.ShareDao;
import com.huatu.ztk.pc.util.RestTemplateUtil;
import com.huatu.ztk.report.bean.PowerSummary;
import com.huatu.ztk.report.bean.PracticeSummary;
import com.huatu.ztk.report.dubbo.PowerSummaryDubboService;
import com.huatu.ztk.report.dubbo.PracticeSummaryDubboService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;


/**
 * 华图在线app分享
 */
@Service
public class HuatuShareService {
    private static final Logger logger = LoggerFactory.getLogger(HuatuShareService.class);
    public static final String REPORT_SHARE_TITLE = "我在华图在线做题，晒晒我的能力评估报告！";
    public static final String REPORT_SHARE_DESC = "我已使用华图在线做题%s天。通过华图在线的智能评估预算，我参加考试的预测分为%s分";
    ;
    public static final String PRACTICE_SHARE_DESC = "我在华图在线进行了一次行测练习：%s，共答对了%s道题，用时%s分%s秒。";
    public static final String PRACTICE_SHARE_TITLE = "我在华图在线做题，每天进步看得见！";
    public static final String COURSE_SHARE_TITLE = "【华图在线】%s";
    public static final String COURSE_SHARE_DESC = "华图在线直播课程，汇聚名师大咖，为你学习路上保驾护航。";
    public static final String ARENA_SHARE_RECORD_TITLE = "华图在线竞技赛场，真正的公考实力较量！";
    public static final String ARENA_SHARE_RECORD_DESC = "我在华图在线竞技赛场PK，真正的较量才刚刚开始！你敢来吗？";
    public static final String ARENA_SHARE_SUMMARY_TITLE = "华图在线公考对战，来一场真正的较量吧！";
    public static final String ARENA_SHARE_SUMMARY_DESC = "我在华图在线竞技赛场PK，真正的较量才刚刚开始！你敢来吗？";
    public static final String ARENA_SHARE_TODAYRANK_TITLE = "华图在线竞技赛场，真正的公考实力较量！";
    public static final String ARENA_SHARE_TODAYRANK_DESC = "我在华图在线竞技赛场PK，今日排行前三名哦！你敢来挑战吗？";

    public static final String MATCH_SHARE_DESC = "我在华图在线参加了一次%s，共答对了%s道题，用时%s分%s秒。";
    public static final String MATCH_SHARE_TITLE = "【华图在线】模考大赛，见证你的进步！";

    public static final String MATCH_SHARE_LINE_TEST = "我在华图在线参加了一次行测模考大赛，共答对了%s道题，用时%s分%s秒。";
    public static final String MATCH_SHARE_ESSAY = "我在华图在线参加了一次申论模考大赛，得分%s分，用时%s分%s秒。";
    public static final String MATCH_SHARE_TOTAL = "我在华图在线参加了一次行测申论联合模考大赛，总分%s分，行测%s分，申论%s分。";

    //红包分享相关 title、desc
    public static final String RED_PACKAGE_TITLE = "你的好友%s给你了一个现金大红包，赶快戳";
    public static final String RED_PACKAGE_DESC = "快来瓜分%s元红包，手快有，手慢无~";

    public static String NS_HUATU_COM = "ns.huatu.com";
    public static String RED_NS_HUATU_COM = "http://ns.huatu.com/common-app/template/wx-red/";
    public static String M_NS_HUATU_COM = "http://m.v.huatu.com/cla/class_detail_%s.html";
    public static String M_VIDEO_SHARE_URL = "http://m.v.huatu.com/mk/audio.html?netClassId=%s&shareSyllabusId=%s&terminal=%s&cv=%s";
    static {
        final String env = System.getProperty("disconf.env");
        //说明是测试环境,设置测试环境地址
        if (env.equalsIgnoreCase("qa")) {
            RED_NS_HUATU_COM = "http://tkproc.huatu.com/common-app/template/wx-red/";
            NS_HUATU_COM = "weixin.htexam.com";
            M_NS_HUATU_COM = "http://testm.v.huatu.com/cla/class_detail_%s.html";
            M_VIDEO_SHARE_URL = "http://testm.v.huatu.com/mk/audio.html?netClassId=%s&shareSyllabusId=%s&terminal=%s&cv=%s";
        }
    }

    @Autowired
    private PracticeCardDubboService practiceCardDubboService;

    @Autowired
    private PracticeSummaryDubboService practiceSummaryDubboService;

    @Autowired
    private ShareDao shareDao;

    @Autowired
    private PowerSummaryDubboService powerSummaryDubboService;

//    @Autowired
//    private ArenaDubboService arenaDubboService;

    @Autowired
    private MatchDao matchDao;

    @Autowired
    private ShareService shareService;

    /**
     * 分享试题
     *
     * @param qid
     * @param shareFlag
     * @return
     */
    public Share shareQuestion(int qid, boolean shareFlag) {
        final Share share = Share.builder()
                .id(generaChareId())
                .desc("下载华图在线，听每天免费的公考直播课。")
                .title("公考路上的小伙伴，来道华图在线的题PK下吧！")
                .url("http://" + NS_HUATU_COM + "/pc/v2/share/question/" + qid)
                .build();
        if(shareFlag){
            shareDao.insert(share);
        }
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
        Share share;

        /**
         * 修改分项名称 by lijun 2018-1-26
         * 通过 AnswerCardType.getTypeName
         */
        share = Share.builder()
                .title(PRACTICE_SHARE_TITLE)
                .desc(String.format(MATCH_SHARE_DESC, AnswerCardType.getTypeName(answerCard.getType()), answerCard.getRcount(), minutes, seconds))
                .type(ShareType.SHARE_PRACTICE)
                .outerId(practiceId + "," + catgory)
                .url("http://" + NS_HUATU_COM + "/pc/v2/share/practice/" + id)
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
                .url("http://" + NS_HUATU_COM + "/pc/v2/share/myreport/" + id)
                .id(id)
                .build();
        shareDao.insert(share);
        return share;
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
                .title(String.format(COURSE_SHARE_TITLE, shareService.getCourseNameV2(courseId)))
                .desc(COURSE_SHARE_DESC)
                .type(ShareType.SHARE_COURSE)
                .outerId(courseId + "," + uid + "," + catgory)
//                .url("http://" + NS_HUATU_COM + "/pc/v2/share/course/" + id)
                .url(String.format(M_NS_HUATU_COM,courseId))
                .id(id)
                .build();
        shareDao.insert(share);
        return share;
    }


    private String generaChareId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 分享竞技成绩统计
     *
     * @param uid
     * @param roomid
     * @return
     */
    public Share shareArenaRecord(long uid, long roomid) throws BizException {
//        ArenaRoom arenaRoom = arenaDubboService.findById(roomid);
//        if (arenaRoom == null) {
//            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
//        }
//        if (!arenaRoom.getPlayerIds().contains(uid)) {//只能分享自己的
//            throw new BizException(CommonErrors.PERMISSION_DENIED);
//        }
        String shareid = generaChareId();
        final Share share = Share.builder()
                .title(ARENA_SHARE_RECORD_TITLE)
                .desc(ARENA_SHARE_RECORD_DESC)
                .type(ShareType.SHARE_ARENA_RECORD)
                .outerId(roomid + "," + uid)
                .url("http://" + NS_HUATU_COM + "/pc/v2/share/arena/record/" + shareid)
                .id(shareid)
                .build();
        shareDao.insert(share);
        return share;
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
                .url("http://" + NS_HUATU_COM + "/pc/v2/share/arena/summary/" + shareid)
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
                .url("http://" + NS_HUATU_COM + "/pc/v2/share/arena/todayrank/" + shareid)
                .id(shareid)
                .build();
        shareDao.insert(share);
        return share;
    }

    public Match findMatchById(int paperId) {
        return matchDao.findById(paperId);
    }

    public Share shareMatch(int paperId,boolean flag) {
        Match match = matchDao.findById(paperId);
        String title = "【华图在线】模考大赛";
        if (match != null) {
            title = "【华图在线】" + match.getName();
        }

        Share share = Share.builder()
                .id(generaChareId())
                .title(title)
                .url("http://" + NS_HUATU_COM + "/pc/v2/share/match/" + paperId)
                .desc("实战考场，预见趋势，权威名师深度解析，备考全阶段模拟，见证你的成长！")
                .build();
        //模考大赛首页分享数据不用存储，没有区别
        if(flag){
            shareDao.insert(share);
        }


        return share;
    }

    /**
     * 处理模考大赛分享信息的接口
     *
     * @param paperId
     * @param uid
     * @param type    需要分享的类型（1行测2申论3总体成绩）
     * @param token
     * @param catgory @return
     */
    public Share sharePracticeWithEssay(long paperId, long uid, int type, String token, int catgory) throws BizException {
        Share share = new Share();
        switch (type) {
            case 1: {
                share = getShareReportOnlyLineTest(paperId, token, uid, catgory);
                break;
            }
            case 2: {
                share = getShareReportOnlyEssay(paperId, token, catgory);
                break;
            }
            case 3: {
                share = getShareReportLineTestWithEssay(paperId, token, catgory);
                break;
            }
            default:
                throw new BizException(CommonErrors.INVALID_ARGUMENTS);
        }
        shareDao.insert(share);
        return share;
    }

    private Share getShareReportLineTestWithEssay(long paperId, String token, int catgory) {
        Map resultMap = RestTemplateUtil.getTotalReportByClient(paperId, token);
        resultMap.put("matchMark", "total");
        String id = generaChareId();
        Share share = Share.builder()
                .title(MATCH_SHARE_TITLE)
                .desc(String.format(MATCH_SHARE_TOTAL, resultMap.get("score"), resultMap.get("lineTestScore"), resultMap.get("essayScore")))
                .type(ShareType.SHARE_PRACTICE)
                .outerId(paperId + "," + catgory + "," + ShareReportType.LINETESTWITHESSAY)
                .url("http://" + NS_HUATU_COM + "/pc/v3/share/practice/" + id)
                .id(id)
                .reportInfo(resultMap)
                .build();
        return share;
    }

    private Share getShareReportOnlyEssay(long paperId, String token, int catgory) throws BizException {
        try {
            Map resultMap = RestTemplateUtil.getEssayReportByClient(paperId, token, "6.0", "2");
            resultMap.put("matchMark", "essay");
            String id = generaChareId();
            Share share = Share.builder()
                    .title(MATCH_SHARE_TITLE)
                    .desc(String.format(MATCH_SHARE_ESSAY, resultMap.get("score"), resultMap.get("minutes"), resultMap.get("seconds")))
                    .type(ShareType.SHARE_PRACTICE)
                    .outerId(paperId + "," + catgory + "," + ShareReportType.ESSAYONLY)
                    .url("http://" + NS_HUATU_COM + "/pc/v3/share/practice/" + id)
                    .reportInfo(resultMap)
                    .id(id)
                    .build();
            return share;
        } catch (Exception e) {
            logger.error("查询模考申论信息报告出错：" + e.getMessage());
            e.printStackTrace();
            throw new BizException(CommonErrors.SERVICE_INTERNAL_ERROR);
        }
    }


    public Share getShareReportOnlyLineTest(long paperId, String token, long uid, int catgory) throws BizException {
        MatchUserMeta userMeta = matchDao.findMatchUserMeta(uid, new Long(paperId).intValue());
        if (userMeta == null) {
            logger.error("userMeta is null");
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }
        long practiceId = userMeta.getPracticeId();
        if (practiceId <= 0) {
            logger.error("practice is null");
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }
        final AnswerCard answerCard = practiceCardDubboService.findById(practiceId);
        if (answerCard == null || answerCard.getUserId() != uid) {//只能分享自己的
            logger.error("answerCard is null Or not match to user");
            throw new BizException(CommonErrors.PERMISSION_DENIED);
        }
        String id = generaChareId();
        final Duration duration = Duration.ofSeconds(answerCard.getExpendTime());
        long minutes = duration.toMinutes(); //75
        final long seconds = duration.minusMinutes(minutes).getSeconds();
        Share share = Share.builder()
                .title(MATCH_SHARE_TITLE)
                .desc(String.format(MATCH_SHARE_LINE_TEST, answerCard.getRcount(), minutes, seconds))
                .type(ShareType.SHARE_PRACTICE)
                .outerId(practiceId + "," + catgory + "," + ShareReportType.LINETESTONLY)
                .url("http://" + NS_HUATU_COM + "/pc/v3/share/practice/" + id)
                .id(id)
                .build();
        return share;
    }

    /**
     * 获取红包分享的对象数据
     */
    public Share getRedPackageShare(long userId,String userName,String avatar,String param,long redPackageId,String moneyNum){
        String id = generaChareId();
        HashMap<String, Object> map = new HashMap<>();
        map.put("redPackageId",redPackageId);
        map.put("userId",userId);
        map.put("userName",userName);
        map.put("param",param);
        map.put("avatar",avatar);
        String mapJson = JsonUtil.toJson(map);

        final Share share = Share.builder()
                .id(id)
                .title(String.format(RED_PACKAGE_TITLE, userName))
                .desc(String.format(RED_PACKAGE_DESC, moneyNum))
                .type(ShareType.SHARE_RED_PACKAGE)
                .outerId(mapJson)
                .url(RED_NS_HUATU_COM+"wx-red-envelope.html")
                .build();
        shareDao.insert(share);
        return share;
    }

    /**
     * 根据ID 查询红包详情
     */
    public HashMap getRedPackageShareById(String id){
        final HashMap<String, Object> map = new HashMap<>();
        Share share = shareDao.findById(id);
        if(null != share){
            Consumer<HashMap<String,Object>> putParam = (mapParam) ->
                    mapParam.entrySet().forEach(entry -> map.put(entry.getKey(),entry.getValue()));
            String shareJson = JsonUtil.toJson(share);
            HashMap<String,Object> toMap = (HashMap)JsonUtil.toMap(shareJson);
            putParam.accept(toMap);
            if (StringUtils.isNotBlank(share.getOuterId())){
                HashMap outerMap = (HashMap)JsonUtil.toMap(share.getOuterId());
                putParam.accept(outerMap);
            }
        }
        return map;
    }
}