package com.huatu.ztk.paper.controller.v3;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.paper.bean.EstimatePaper;
import com.huatu.ztk.paper.bean.Match;
import com.huatu.ztk.paper.common.MatchRedisKeys;
import com.huatu.ztk.paper.common.MatchStatus;
import com.huatu.ztk.paper.common.PaperType;
import com.huatu.ztk.paper.common.PastMatchCourseConfig;
import com.huatu.ztk.paper.service.MatchService;
import com.huatu.ztk.paper.service.PaperRewardService;
import com.huatu.ztk.user.service.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhouwei
 */

@RestController
@RequestMapping(value = "/v3/matches", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class MatchControllerV3 {
    private static final Logger logger = LoggerFactory.getLogger(MatchControllerV3.class);

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private MatchService matchService;

    @Autowired
    private PaperRewardService paperRewardService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    PastMatchCourseConfig pastMatchCourseConfig;

    /**
     * 模考大赛入口接口
     *
     * @param token
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public Object getMatch(@RequestHeader(required = false) String token,
                           @RequestParam(defaultValue = "false") boolean flag) throws BizException {
        //验证token
        logger.info("v3/matches,token={}", token);
        userSessionService.assertSession(token);
        long userId = userSessionService.getUid(token);
        int subject = userSessionService.getSubject(token);
        //降级处理逻辑
        if (matchService.isDegrade() || flag) {
            return matchService.getMatchHeaderMock(userId, subject);
        }
        //  查当前科目下的模考大赛
        List<Match> matches = matchService.getMatchesWithEssay(userId, subject);
        /**
         * updateBy lijun 2018-02-26
         * 此部分代码只作为 内部线上数据测试使用,与原始业务逻辑相违背.
         * 白名单 状态值 矫正
         */
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
                                @RequestParam(defaultValue = "1") int tag,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "20") int size) throws BizException {
        logger.info("token={},tag={}", token, tag);
        userSessionService.assertSession(token);
        /**
         * 客户端下页不加载问题解决
         */
        size = 50;
        if (page <= 0) {
            throw new BizException(ErrorResult.create(1000123, "页数参数非法"));
        }
        long userId = userSessionService.getUid(token);
        int subject = userSessionService.getSubject(token);
        //发现科目是14申论（弃用），改成查询行测科目下信息
        if (14 == subject) {
            subject = 1;
        }
        //查询所有已结束的模考大赛（包括没出报告的）
        long total = matchService.findPastMatchesTotal(tag, subject);
        List<EstimatePaper> list = matchService.findPastMatches(tag, userId, page, size, subject);
        list.forEach(i -> i.setType(PaperType.MATCH_AFTER));
        Map<String, Object> mapData = Maps.newHashMap();
        mapData.put("result", list);
        mapData.put("total", total);
        mapData.put("next", page * size > total ? 0 : 1);
        return mapData;
    }

    /**
     * 模考历史,不做分页
     *
     * @param token
     * @param tag
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "history", method = RequestMethod.GET)
    public Object getHistory(@RequestHeader(required = false) String token,
                             @RequestParam(defaultValue = "1") int tag,
                             @RequestHeader(defaultValue = "1") int terminal,
                             @RequestHeader(defaultValue = "1") String cv) throws BizException {
        logger.info("token={},tag={}", token, tag);
        userSessionService.assertSession(token);
        long userId = userSessionService.getUid(token);
        int subject = userSessionService.getSubject(token);
        //发现科目是14申论（弃用），改成查询行测科目下信息
        if (14 == subject) {
            subject = 1;
        }
        return matchService.getHistoryWithEssayV62(userId, tag, subject, terminal, cv);
    }

    /**
     * 模考大赛报名
     * version 3
     * add '招警机考' match(招警-行测专用)
     * note!!<add schoolId />
     */
    @RequestMapping(value = "{paperId}", method = RequestMethod.POST)
    public Object enroll(@PathVariable int paperId,
                         @RequestHeader(required = false) String token,
                         @RequestParam(defaultValue = "-9") int positionId,
                         @RequestParam(defaultValue = "") Long schoolId,
                         @RequestParam(defaultValue = "") String schoolName) throws BizException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        userSessionService.assertSession(token);
        long userId = userSessionService.getUid(token);
        logger.info("【REQUEST】招警机考报名。paperId={},userId={},positionId={},schoolId={},schoolName={}", paperId, userId, positionId, schoolId, schoolName);


        matchService.enrollV3(paperId, userId, positionId, schoolId, schoolName);
        paperRewardService.sendEnrollMsg(userId, userSessionService.getUname(token), paperId);

        logger.info("【RESPONSE】招警机考报名。接口响应用时={}", String.valueOf(stopwatch.stop()));
        return SuccessMessage.create("报名成功!");
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
        userSessionService.assertSession(token);
        long userId = userSessionService.getUid(token);
        logger.info("【REQUEST】招警机考首页。userId={},terminal={},subjectId={}", userId, terminal, subjectId);

        /*  查招警科目下的模考大赛 */
        Match match = matchService.getMatchMachineMatchV3(userId, 100100173);
        logger.info("【RESPONSE】招警机考首页。接口响应用时={}, match={}", String.valueOf(stopwatch.stop()), match);
        return match;
    }

    /**
     * type 标签类型（跟模考大赛标签一一对应） 1 2019年国考 2 2019年省考 -1 全部
     *
     * @param token
     * @param type
     * @return
     */
    @RequestMapping(value = "matchCourseInfo", method = RequestMethod.GET)
    public Object getMatchCollectionCourseInfo(@RequestHeader(required = false) String token,
                                               @RequestParam(defaultValue = "1") int type) throws BizException {
        userSessionService.assertSession(token);
        int subjectId = userSessionService.getSubject(token);
        String pastMatchCourseJson = pastMatchCourseConfig.getPastMatchCourseJson();
        logger.info("往期模考配置信息是:{}", pastMatchCourseJson);
        List<HashMap> mapList = JsonUtil.toList(pastMatchCourseJson, HashMap.class);
        mapList = mapList.stream().filter(map -> map.get("subjectId").equals(subjectId) && map.get("tag").equals(type))
                .collect(Collectors.toList());
        return mapList;
    }
}
