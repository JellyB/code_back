package com.huatu.tiku.essay.web.controller.api.V3;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.huatu.common.CommonResult;
import com.huatu.common.SuccessMessage;
import com.huatu.common.exception.BizException;
import com.huatu.common.spring.event.EventPublisher;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.constant.cache.RedisKeyConstant;
import com.huatu.tiku.essay.constant.match.MatchRedisKeyConstant;
import com.huatu.tiku.essay.entity.EssayMockExam;
import com.huatu.tiku.essay.entity.vo.report.Match;
import com.huatu.tiku.essay.entity.vo.report.MatchUserMeta;
import com.huatu.tiku.essay.service.EssayMatchService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.springboot.basic.reward.RewardAction;
import com.huatu.tiku.springboot.basic.reward.event.RewardActionEvent;
import com.huatu.tiku.springboot.users.support.Token;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by x6 on 2017/12/15.
 * 模考相关
 */
@RestController
@Slf4j
@RequestMapping(value = "api/v3/mock", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class ApiMockExamControllerV3 {


    @Autowired
    EssayMatchService essayMatchService;
    @Autowired
    private EventPublisher eventPublisher;
    @Autowired
    RedisTemplate<String, Object> redisTemplate;
    @Value("${extra_time}")
    private long extraTime;
    @Value("${enterLimitTime}")
    private Integer enterLimitTime;
    @Value("${commitLimitTime}")
    private Integer commitLimitTime;
//    距离可以查看试题信息XXmin时，按钮置成灰色的”开始考试”。
    @Value("${startLimitTime}")
    private Integer startLimitTime;

    /**
     * 模考大赛入口接口
     *
     * @param userSession
     * @return
     * @throws BizException
     */
    @LogPrint
    @RequestMapping(value = "", method = RequestMethod.GET)
    public Object getMatch(@Token UserSession userSession) throws BizException {
        int userId = -1;
        String mockDegradeSwitchKey = MatchRedisKeyConstant.getMockDegradeSwitch();
        Object mockDegradeSwitch = redisTemplate.opsForValue().get(mockDegradeSwitchKey);
        //未降级
        if(null == mockDegradeSwitch){
            userId = userSession.getId();
            // 查当前可见的模考大赛(单独的申论模考&&和行测绑定的申论模考)
            List<EssayMockExam> matches = essayMatchService.getCurrent();

            //为了适配模考顺序问题临时添加
            //Collections.reverse(matches);
            
            LinkedList<Match> list = new LinkedList<>();
            if (CollectionUtils.isNotEmpty(matches)) {

                for (EssayMockExam mock : matches) {
                    //题冲模考信息和用户报名信息
                    Match match = EssayMockUtil.packMatchInfo(mock, userId ,
                            enterLimitTime,commitLimitTime,startLimitTime,
                            essayMatchService,redisTemplate,
                            extraTime);

                    //设置match的flag标识（）
                    EssayMockUtil.checkMatchFlag(match);
                    //当状态为未报名和停止报名时，stage 统一置为 0
                    match.setStage(2);
                    list.add(match);
                }

            }
            return list;
        }else{
            //降级（返回固定信息,读redis）
            String mockDegradeListKey = MatchRedisKeyConstant.getMockDegradeList();
            Object mockDegradeList = redisTemplate.opsForValue().get(mockDegradeListKey);
            if(null == mockDegradeList){
                List<EssayMockExam> matches = essayMatchService.getCurrent();
                LinkedList<Match> list = new LinkedList<>();
                if (CollectionUtils.isNotEmpty(matches)) {

                    for (EssayMockExam mock : matches) {
                        String enrollCountKey = RedisKeyConstant.getTotalEnrollCountKey(mock.getId());
                        Integer enrollCount = (Integer) redisTemplate.opsForValue().get(enrollCountKey);
                        //题冲模考信息和用户报名信息
                        Match match = Match.builder()
                                //模考id
                                .essayPaperId(mock.getId())
                                //模考名称
                                .name(mock.getName())
                                //模考时间信息
                                .timeInfo(EssayMockUtil.getTimeInfo(mock.getStartTime().getTime(), mock.getEndTime().getTime()))
                                //解析课信息
                                .courseId(mock.getCourseId())
                                .courseInfo(mock.getCourseInfo())
                                //考试说明
                                .instruction(mock.getInstruction())
                                .instructionPC(mock.getInstructionPC())
                                //暂时  4 2019申论考试
                                .tag(3)
                                .essayStartTime(mock.getStartTime().getTime())
                                .essayEndTime(mock.getEndTime().getTime())
                                //1只有行测报告2只有申论报告3行测申论报告都有
                                .flag(2)
                                //报名人数(positionCount放的是总报名人数)
                                .enrollCount(enrollCount == null ? 0 : enrollCount)
                                .status(2)
                                .stage(2)
                                /**
                                 * add by zhaoxi (v7.1.12新需求，添加答题交卷时间限制)
                                 */
                                .enterLimitTime(enterLimitTime)
                                .commitLimitTime(commitLimitTime)
                                .build();
                        MatchUserMeta userMeta = MatchUserMeta.builder()
                                .paperId(mock.getId().intValue())
                                .positionCount(1)
                                .positionId(1L)
                                .practiceId(0L)
                                .positionName("北京")
                                .build();
                        match.setUserMeta(userMeta);
                        list.add(match);
                    }

                }
                redisTemplate.opsForValue().set(mockDegradeListKey,list,3,TimeUnit.MINUTES);
                return list;
            }else{
                return mockDegradeList;
            }
        }

    }

    /**
     * 模考大赛报名
     *
     * @param paperId
     * @param positionId
     * @return
     * @throws BizException
     */
    @LogPrint
    @RequestMapping(value = "", method = RequestMethod.POST)
    public Object enroll(@RequestParam(defaultValue = "-9") long paperId,
                         @Token UserSession userSession,
                         @RequestParam(defaultValue = "-9") int positionId) throws BizException {

        //参数校验
        if (paperId <= 0) {
            log.error("请求参数异常，模考ID不能为空");
            throw new BizException(CommonResult.INVALID_ARGUMENTS);

        }

        //报名流程
        essayMatchService.enroll(paperId, userSession, positionId);
        //金币奖励发送
        eventPublisher.publishEvent(RewardActionEvent.class,
                this,
                (event) -> event.setAction(RewardAction.ActionType.MATCH_ENROLL)
                        .setUid(userSession.getId())
                        .setUname(userSession.getUname())
        );
        return SuccessMessage.create("报名成功!");
    }

    /**
     * 模考大赛入口接口（pc首页使用）
     *
     * @param userSession
     * @return
     * @throws BizException
     */
    @LogPrint
    @RequestMapping(value = "page", method = RequestMethod.GET)
    public Object getMatch(@Token(check = false) UserSession userSession,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "20") int pageSize) throws BizException {

        int userId = -1;
        if (null != userSession) {
            userId = userSession.getId();
        }

        // 查当前可见的模考大赛(单独的申论模考&&和行测绑定的申论模考)
        PageUtil currentPage = essayMatchService.getCurrentPage(page, pageSize);
        List<EssayMockExam> matches = new LinkedList<>();

        if (null != currentPage) {
            matches = (List<EssayMockExam>) currentPage.getResult();
        }
        Map<String, Map> mockCourseMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(matches)) {
            List<Integer> mockCourseIdList = matches.stream().map(EssayMockExam::getCourseId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(mockCourseIdList)) {
                StringBuilder mockCourseIdStr = new StringBuilder();
                for (Integer mockCourseId : mockCourseIdList) {
                    if (mockCourseIdStr.length() > 0) {
                        mockCourseIdStr.append(",");
                    }
                    mockCourseIdStr.append(mockCourseId);
                }
                mockCourseMap = essayMatchService.getMockCourseList(mockCourseIdStr.toString());

            }
            List<Match> list = new LinkedList<>();
            for (EssayMockExam mock : matches) {
                //题冲模考信息和用户报名信息
                Match match = EssayMockUtil.packMatchInfo(mock, userId,
                        enterLimitTime, commitLimitTime,
                        startLimitTime, essayMatchService, redisTemplate,
                        extraTime);
                //设置match的flag标识（）
                EssayMockUtil.checkMatchFlag(match);
                //当状态为未报名和停止报名时，stage 统一置为 0
                match.setStage(2);
                //填充解析课信息
                Map course = mockCourseMap.get(match.getCourseId() + "");
                match.setLiveDate(null != course && null != course.get("liveDate") ? Long.parseLong(course.get("liveDate").toString()) : 0L);
                match.setPrice(null != course && null != course.get("price") ? Integer.parseInt(course.get("price").toString()) : 0);

                list.add(match);
            }
            currentPage.setResult(list);
        }
        return currentPage;
    }

}
