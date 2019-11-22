package com.huatu.ztk.paper.controller.v4;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.paper.bean.EstimatePaper;
import com.huatu.ztk.paper.bean.Match;
import com.huatu.ztk.paper.bean.PaperVo;
import com.huatu.ztk.paper.bean.PastMatchCourseInfo;
import com.huatu.ztk.paper.common.MatchRedisKeys;
import com.huatu.ztk.paper.common.MatchStatus;
import com.huatu.ztk.paper.common.PaperType;
import com.huatu.ztk.paper.common.PastMatchCourseConfig;
import com.huatu.ztk.paper.service.MatchService;
import com.huatu.ztk.paper.service.PaperService;
import com.huatu.ztk.user.service.UserSessionService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.http.MediaType;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author huangqp
 */

@RestController
@RequestMapping(value = "/v4/matches", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class MatchControllerV4 {
    private static final Logger logger = LoggerFactory.getLogger(MatchControllerV4.class);

    @Autowired
    private UserSessionService userSessionService;
    @Autowired
    private MatchService matchService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private PastMatchCourseConfig pastMatchCourseConfig;
    @Autowired
    private PaperService paperService;

    /**
     * 模考大赛入口接口（返回空值，不再报错，改为返回空数组）
     *
     * @param token
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public Object getMatch(@RequestHeader(required = false) String token,
                           @RequestParam(defaultValue = "-1") int subjectId,
                           @RequestParam(defaultValue = "false") boolean flag ) throws BizException {
        //验证token
        logger.info("v4/matches,token={},subjectId={}", token,subjectId);
        userSessionService.assertSession(token);
        long userId = userSessionService.getUid(token);
        int subject = 0;
        if(subjectId<0){
            subject = userSessionService.getSubject(token);
        }else{
            subject = subjectId;
        }
        //降级处理逻辑
        if(matchService.isDegrade()||flag){
            return matchService.getMatchHeaderMock(userId,subject);
        }
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("getMatchesWithEssay");
        //  查当前科目下的模考大赛()
        List<Match> matches = null;
        try {
            matches = matchService.getMatchesWithEssay(userId, subject);
            if (CollectionUtils.isEmpty(matches)) {
                return Lists.newArrayList();
            }
        } catch (BizException e) {
            logger.error("error={}", e.getErrorResult().getMessage());
            return Lists.newArrayList();
        }
        /**
         * updateBy lijun 2018-02-26
         * 此部分代码只作为 内部线上数据测试使用,与原始业务逻辑相违背.
         * 白名单 状态值 矫正
         */
        stopWatch.stop();
        stopWatch.start("getMatchWhitUserReport");
        SetOperations opsForSet = redisTemplate.opsForSet();
        Boolean member = opsForSet.isMember(MatchRedisKeys.getMatchWhitUserReportKey(), String.valueOf(userId));
        if (member) {
            matches = matches.stream().map(match -> {
                if (match.getStatus() == MatchStatus.ENROLL
                        || match.getStatus() == MatchStatus.START_UNAVILABLE) {

                    //当前不能考试时, 修改状态 成可考试状态
                    match.setStatus(MatchStatus.START_AVILABLE);
                }
                if (match.getStatus() == MatchStatus.REPORT_UNAVILABLE) {
                    //当前不能查看报告,修改状态 成 可查看报告
                    match.setStatus(MatchStatus.REPORT_AVAILABLE);
                }

                return match;
            }).collect(Collectors.toList());
        }
        //update end
        stopWatch.stop();
        if(stopWatch.getTotalTimeMillis()>1000){
            logger.info("模考大赛非降级逻辑耗时1：{}",stopWatch.getTotalTimeMillis());
            logger.info(stopWatch.prettyPrint());
        }
        return matches;
    }


    /**
     * 模考大赛往期试卷
     *
     * @param token
     * @param tag
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "past", method = RequestMethod.GET)
    public Object getMatchPaper(@RequestHeader(required = false) String token,
                                @RequestHeader(defaultValue = "-1") int subject,
                                @RequestParam(defaultValue = "-1") int subjectId,
                                @RequestParam(defaultValue = "1") int tag,
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "20") int size) throws BizException {
        logger.info("token={},tag={}", token, tag);
        Map<String, Object> mapData = Maps.newHashMap();
        if(subjectId > 0){
            subject = subjectId;
        }
        if (StringUtils.isNotBlank(token)) {
            userSessionService.assertSession(token);
            long userId = userSessionService.getUid(token);
            if (subject == -1) {
                subject = userSessionService.getSubject(token);
            }
            List<PaperVo> list = matchService.findPastMatchesWithStatics(tag, userId, page, size, subject);
            mapData.put("result", list);
        } else {
            //游客模式
            List<PaperVo> list = matchService.findPastMatchesWithStatics(tag, -1L, page, size, subject);
            mapData.put("result", list);
            mapData.put("subject", subject);
        }
        long total = matchService.findPastMatchesTotal(tag, subject);
        mapData.put("total", total);
        mapData.put("next", page * size > total ? 0 : 1);
        return mapData;
    }

    /**
     * 往期模考合集课程接口
     *
     * @param subject
     * @param tag
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "matchCourseInfo", method = RequestMethod.GET)
    public Object getMatchCollectionCourseInfo(@RequestParam(defaultValue = "-1") int subject,
                                               @RequestParam(defaultValue = "1") int tag) throws BizException {
        String pastMatchCourseJson = pastMatchCourseConfig.getPastMatchCourseJson();
        List<HashMap> mapList = JsonUtil.toList(pastMatchCourseJson, HashMap.class);
        logger.info("pastMatchCourseJson={}",pastMatchCourseJson);
        final int subjectId = subject;
        Optional<HashMap> first = mapList
                .stream()
                .filter(map -> MapUtils.getInteger(map, "subjectId").intValue() == subjectId && MapUtils.getInteger(map, "tag").intValue() == tag)
                .findFirst();
        if (first.isPresent()) {
            HashMap hashMap = first.get();
            return JSONObject.parseObject(JSONObject.toJSONString(hashMap), PastMatchCourseInfo.class);
        } else {
            return new PastMatchCourseInfo();
        }
    }

    /**
     * 模考大赛入口接口(招警机考)
     *
     * @param token
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "machine", method = RequestMethod.GET)
    public Object getMachineMatch(@RequestHeader(required = false) String token,
                                  @RequestHeader int terminal,
                                  @RequestParam(defaultValue = "-1") int subjectId) throws BizException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        if (StringUtils.isBlank(token)) {
            return matchService.getMatchMachineMatchV4(-1L, 100100173);
        }
        userSessionService.assertSession(token);
        long userId = userSessionService.getUid(token);
        logger.info("【REQUEST】招警机考首页。userId={},terminal={},subjectId={}", userId, terminal, subjectId);

        /*  查招警科目下的模考大赛 */
        List<Match> match = matchService.getMatchMachineMatchV4(userId, 100100173);
        logger.info("【RESPONSE】招警机考首页。接口响应用时={}, match={}", String.valueOf(stopwatch.stop()), match);
        return match;
    }

    @RequestMapping(value = "old/flag", method = RequestMethod.GET)
    public Object getMatchOldFlag(@RequestHeader(required = false) String token,
                                  @RequestHeader int terminal,
                                  @RequestHeader(defaultValue = "-1") int subject,
                                  @RequestHeader(defaultValue = "1") String cv) throws BizException {
        logger.info("getMatchOldFlag, token={},terminal={},subjectId={},cv={}", token, terminal, subject, cv);
        if (subject < 0) {
            userSessionService.assertSession(token);
            subject = userSessionService.getSubject(token);
        }
        return matchService.getMatchOldFlag(terminal, subject, cv);
    }

    /**
     * 白名单试卷列表（）
     *
     * @param token
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "look", method = RequestMethod.GET)
    public Object list(@RequestHeader(required = false) String token,
                           @RequestParam(defaultValue = "-1") int subjectId,
                           @RequestParam(defaultValue = "false") boolean flag ) throws BizException {
        long userId = userSessionService.getUid(token);
        if(subjectId == -1){
            subjectId = userSessionService.getSubject(token);
        }
        //验证token
        logger.info("v4/matches,token={},subjectId={}", token,subjectId);
        SetOperations opsForSet = redisTemplate.opsForSet();
        Boolean member = opsForSet.isMember(MatchRedisKeys.getMatchWhitUserReportKey(), String.valueOf(userId));
        if (member) {
            List<EstimatePaper> result = paperService.findByUidAndType(PaperType.MATCH, subjectId);
            return result.stream().map(i->{
                HashMap<Object, Object> tempMap = Maps.newHashMap();
                tempMap.put("id",i.getId());
                tempMap.put("name",i.getName());
                tempMap.put("questions",i.getQuestions());
                return tempMap;
            }).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

}
