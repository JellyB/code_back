package com.huatu.ztk.paper.controller;

import com.google.common.collect.Lists;
import com.huatu.ztk.commons.CatgoryType;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.paper.bean.Match;
import com.huatu.ztk.paper.common.MatchConfig;
import com.huatu.ztk.paper.common.MatchRedisKeys;
import com.huatu.ztk.paper.common.MatchStatus;
import com.huatu.ztk.paper.service.MatchService;
import com.huatu.ztk.paper.service.PaperRewardService;
import com.huatu.ztk.user.service.UserSessionService;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhouwei
 * 2017-12-25 16:24:21
 */

@RestController
@RequestMapping(value = "/v2/matches", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class MatchControllerV2 {
    private static final Logger logger = LoggerFactory.getLogger(MatchControllerV2.class);
    private static List<Map> tags = new ArrayList<>(4);
    public static final int IS_NOT_ESSAY = 0;
    public static final int IS_ESSAY = 1;
    @Autowired
    private MatchConfig matchConfig;
    private static List<Map> teacherTags = new ArrayList<>(5);


    static {
        Map map = new LinkedHashMap();
        map.put("id", 1);
        map.put("name", "2019国考行测");
        map.put("flag", IS_NOT_ESSAY);

        Map map2 = new LinkedHashMap();
        map2.put("id", 2);
        map2.put("name", "2019省考行测");
        map2.put("flag", IS_NOT_ESSAY);


        Map map3 = new LinkedHashMap();
        map3.put("id", 3);
        map3.put("name", "2019申论模考");
        map3.put("flag", IS_ESSAY);
//        Map map4 = new LinkedHashMap();
//        map4.put("id", 4);
//        map4.put("name", "2019国考行测");
        tags.add(map2);
//        tags.add(map4);
        tags.add(map);
        tags.add(map3);
    }


    static {
        Map map = new LinkedHashMap();
        map.put("id", 1);
        map.put("name", "教师资格证");
        map.put("flag", IS_NOT_ESSAY);

        Map map2 = new LinkedHashMap();
        map2.put("id", 2);
        map2.put("name", "教师招聘");
        map2.put("flag", IS_NOT_ESSAY);

        Map map3 = new LinkedHashMap();
        map3.put("id", 3);
        map3.put("name", "特岗教师");
        map3.put("flag", IS_NOT_ESSAY);

        Map map4 = new LinkedHashMap();
        map4.put("id", 4);
        map4.put("name", "事业单位D类");
        map4.put("flag", IS_NOT_ESSAY);

        teacherTags.add(map2);
        teacherTags.add(map);
        teacherTags.add(map3);
        teacherTags.add(map4);

    }

    @Autowired
    private UserSessionService userSessionService;


    @Autowired
    private MatchService matchService;


    @Autowired
    private PaperRewardService paperRewardService;

    @Autowired
    private RedisTemplate redisTemplate;

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
        logger.info("v2/matches,token={}", token);
        userSessionService.assertSession(token);
        long userId = userSessionService.getUid(token);
        int subject = userSessionService.getSubject(token);
        //降级处理逻辑
        if (matchService.isDegrade() || flag) {
            return matchService.getMatchHeaderMockPc(userId, subject);
        }
        //  查当前科目下的模考大赛
        Match match = matchService.getMatchWithEssay(userId, subject);
        /**
         * updateBy lijun 2018-02-26
         * 此部分代码只作为 内部线上数据测试使用,与原始业务逻辑相违背.
         * 白名单 状态值 矫正
         */
        SetOperations opsForSet = redisTemplate.opsForSet();
        Boolean member = opsForSet.isMember(MatchRedisKeys.getMatchWhitUserReportKey(), String.valueOf(userId));
        if (member) {
            if (match.getStatus() == MatchStatus.ENROLL
                    || match.getStatus() == MatchStatus.START_UNAVILABLE) {

                //当前不能考试时, 修改状态 成可考试状态
                match.setStatus(MatchStatus.START_AVILABLE);
            }
            if (match.getStatus() == MatchStatus.REPORT_UNAVILABLE) {
                //当前不能查看报告,修改状态 成 可查看报告
                match.setStatus(MatchStatus.REPORT_AVAILABLE);
            }
        }

        return match;
    }


    /**
     * 模考大赛报名
     *
     * @param paperId
     * @param token
     * @param positionId 默认-9处理事业单位不选报名地区的问题
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "{paperId}", method = RequestMethod.POST)
    public Object enroll(@PathVariable int paperId,
                         @RequestHeader(required = false) String token,
                         @RequestHeader(required = false, defaultValue = "0") int terminal,
                         @RequestParam(defaultValue = "-9") int positionId) throws BizException {
        logger.info("paperId={},token={},positionId={}", paperId, token, positionId);
        userSessionService.assertSession(token);
        long userId = userSessionService.getUid(token);
        int subject = userSessionService.getSubject(token);

        matchService.enrollV2(paperId, userId, positionId, subject, terminal);

        paperRewardService.sendEnrollMsg(userId, userSessionService.getUname(token), paperId);

        return SuccessMessage.create("报名成功!");
    }

    /**
     * 模考历史,不做分页
     * TODO 不用每次都实时计算，可以直接将结果缓存
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

        return matchService.getHistoryWithEssay(userId, tag, subject, terminal, cv);
    }

    /**
     * 模考大赛历史列表页头部标签(详见package-info)
     *
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "tags", method = RequestMethod.GET)
    public Object getTags(@RequestHeader(required = false) String token) throws BizException {
        String tagInfo = matchConfig.getTagInfo();
        if (StringUtils.isNotBlank(tagInfo)) {
            int subject = userSessionService.getSubject(token);
            List<Map> list = getTagBySubject(tagInfo, subject);
            list.forEach(i -> {
                i.put("flag", i.get("channel"));
            });
            return list;
        }
        int category = userSessionService.getCatgory(token);
        if (category == CatgoryType.GONG_WU_YUAN) {
            return tags;
        } else if (category == CatgoryType.SHI_YE_DAN_WEI) {
            Map map = new LinkedHashMap();
            map.put("id", 1);
            map.put("name", "2018公基");
            map.put("flag", IS_NOT_ESSAY);
            Map map1 = new LinkedHashMap();
            map1.put("id", 12);
            map1.put("name", "2019公基");
            map1.put("flag", IS_NOT_ESSAY);
            return Lists.newArrayList(map1, map);
            //教师的话返回如下tag
        } else if (category == 200100045) {
            return teacherTags;
        }
        return null;

    }

    private List<Map> getTagBySubject(String tagInfo, int subject) {
        List<Map> maps = JsonUtil.toList(tagInfo, Map.class);
        //非申论科目使用科目ID做筛选
        if (subject != 14) {
            List<Map> list = maps.stream()
                    .filter(map -> MapUtils.getInteger(map, "subject") == subject)
                    .collect(Collectors.toList());
            return list;

        }
        //申论科目使用flag  = 1 做筛选，意味着只保留调用申论接口的标签
        List<Map> list = maps.stream()
                .filter(map -> MapUtils.getInteger(map, "channel") == IS_ESSAY)
                .collect(Collectors.toList());
        return list;
    }


    /**
     * 模考大赛历史列表页标签
     * 1 只参加行测 2 只参加申论 3 都参加
     *
     * @param token
     * @param practiceId
     * @param tag        1
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "historyTags", method = RequestMethod.GET)
    public Object historyTags(@RequestHeader String token, @RequestParam long practiceId, @RequestParam int tag) throws BizException {
        logger.info("token={},id={}", token, practiceId);
        userSessionService.assertSession(token);
        long userId = userSessionService.getUid(token);

        return matchService.getHistoryTag(userId, practiceId, tag);
    }
}
