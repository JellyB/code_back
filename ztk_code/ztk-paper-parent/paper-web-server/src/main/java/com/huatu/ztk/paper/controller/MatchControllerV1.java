package com.huatu.ztk.paper.controller;

import com.huatu.ztk.commons.TerminalType;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.paper.bean.Match;
import com.huatu.ztk.paper.common.PositionConstants;
import com.huatu.ztk.paper.service.MatchService;
import com.huatu.ztk.paper.service.PaperRewardService;
import com.huatu.ztk.user.service.UserSessionService;
import com.sensorsdata.analytics.javasdk.SensorsAnalytics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.*;


/**
 * Created by linkang on 17-7-14.
 */
@RestController
@RequestMapping(value = "/v1/matches", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class MatchControllerV1 {
    private static final Logger logger = LoggerFactory.getLogger(MatchControllerV1.class);
    @Autowired
    private UserSessionService userSessionService;


    @Autowired
    private MatchService matchService;


    @Autowired
    private PaperRewardService paperRewardService;

    @Autowired
    private SensorsAnalytics sensorsAnalytics;

    /**
     * 模考大赛入口接口
     *
     * @param token
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public Object getMatch(@RequestHeader(required = false) String token,
                           @RequestHeader int terminal,
                           @RequestParam(defaultValue = "-1") int subjectId,
                           @RequestParam(defaultValue = "false") boolean flag) throws BizException {
        logger.info("v1/matches,token={},terminal={}", token, terminal);
        long userId = -1;
        int subject;
        try {
            userSessionService.assertSession(token);
            userId = userSessionService.getUid(token);
            subject = userSessionService.getSubject(token);
        } catch (BizException e) {
            if (terminal == TerminalType.PC && subjectId != -1) {
                subject = subjectId;
            } else {
                throw e;
            }
        }
        //降级处理逻辑
        if (matchService.isDegrade() || flag) {
            return matchService.getMatchHeaderMockPc(userId, subject);
        }
        /*  查当前科目下的模考大赛 */
        Match match = matchService.getMatch(userId, subject);
        return match;
    }

    /**
     * 模考大赛报名
     *
     * @param paperId
     * @param token
     * @param positionId
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "{paperId}", method = RequestMethod.POST)
    public Object enroll(@PathVariable int paperId,
                         @RequestHeader(required = false, defaultValue = "0") int terminal,
                         @RequestHeader(required = false) String token,
                         @RequestParam(defaultValue = "-9") int positionId) throws BizException {
        userSessionService.assertSession(token);
        long userId = userSessionService.getUid(token);
        int subject = userSessionService.getSubject(token);

//        matchService.enroll(paperId, userId, positionId);
        matchService.enrollV2(paperId, userId, positionId, subject, terminal);

        paperRewardService.sendEnrollMsg(userId, userSessionService.getUname(token), paperId);

        return SuccessMessage.create("报名成功!");
    }


    /**
     * 职位列表
     * 模考大赛报名时，先选择地区和职位（目前隐藏职位）
     *
     * @return
     */
    @RequestMapping(value = "positions", method = RequestMethod.GET)
    public Object getPositions(@RequestHeader(required = false, defaultValue = "0") int terminal) {
        return PositionConstants.getPositions();
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
        userSessionService.assertSession(token);
        long userId = userSessionService.getUid(token);
        int subject = userSessionService.getSubject(token);

        return matchService.getHistory(userId, tag, subject, terminal, cv);
    }

    /**
     * 模考历史,不做分页
     * pc端专用，token中的subject不用使用参数subject
     *
     * @param token
     * @param subjectId
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "history/pc", method = RequestMethod.GET)
    public Object getPCHistory(@RequestHeader(required = false) String token,
                               @RequestParam(required = false) Integer subjectId) throws BizException {
        userSessionService.assertSession(token);
        long userId = userSessionService.getUid(token);
        if (Objects.isNull(subjectId) || subjectId.intValue() <= 0) {
            subjectId = userSessionService.getSubject(token);
        }
        return matchService.getHistory(userId, -1, subjectId, TerminalType.PC, "1");
    }

    /**
     * 模考大赛历史，列表页  头部标签
     *
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "tags", method = RequestMethod.GET)
    public Object getTags() throws BizException {

        Map map = new LinkedHashMap();

        map.put("id", 1);
        map.put("name", "2018国考行测");

        Map map2 = new LinkedHashMap();
        map2.put("id", 2);
        map2.put("name", "2018省考行测");

        List<Map> tags = new ArrayList<>(3);
        tags.add(map2);
        tags.add(map);
        return tags;
    }


    /**
     * 系统当前时间（pc招警机考）
     *
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "time", method = RequestMethod.GET)
    public Object getCurrentTime() {

        Map map = new HashMap<String, Long>();

        map.put("currentTime", System.currentTimeMillis());

        return map;
    }
}
