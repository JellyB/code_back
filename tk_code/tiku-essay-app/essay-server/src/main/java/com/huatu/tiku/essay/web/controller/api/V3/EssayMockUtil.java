package com.huatu.tiku.essay.web.controller.api.V3;

import com.huatu.tiku.essay.constant.cache.RedisKeyConstant;
import com.huatu.tiku.essay.constant.status.EssayAnswerConstant;
import com.huatu.tiku.essay.constant.status.EssayQuestionAnswerConstant;
import com.huatu.tiku.essay.constant.status.MatchStatus;
import com.huatu.tiku.essay.entity.EssayMockExam;
import com.huatu.tiku.essay.entity.vo.report.Match;
import com.huatu.tiku.essay.entity.vo.report.MatchUserMeta;
import com.huatu.tiku.essay.service.EssayMatchService;
import com.huatu.tiku.essay.util.date.DateUtil;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author huangqingpeng
 * @title: EssayMockUtil
 * @description: 模考大赛数据组装
 * @date 2019-07-2915:19
 */
public class EssayMockUtil {

    /**
     * 判断用户考试成绩情况(0表示没有成绩报告1表示只有行测报告2只有申论报告3行测申论报告都有)
     *
     * @param match
     */
    public static void checkMatchFlag(Match match) {
        //默认，没有成绩报告
        match.setFlag(0);
        if (match.getUserMeta() == null) {
            //用户没有报名（没有成绩报告，不处理）
            return;
        } else {
            //申论考试完全结束，并且用户参与考试
            match.setFlag(2);
            return;
        }
    }


    /**
     * 封装模考信息（将申论的mock对象,放入match中）
     *
     * @param mock
     * @param enterLimitTime
     * @param commitLimitTime
     * @param startLimitTime
     * @param essayMatchService
     * @param redisTemplate
     * @param extraTime
     * @return
     */
    public static Match packMatchInfo(EssayMockExam mock, int userId, Integer enterLimitTime, Integer commitLimitTime, Integer startLimitTime, EssayMatchService essayMatchService, RedisTemplate<String, Object> redisTemplate, long extraTime) {
        MatchUserMeta userMeta = null;
        if(userId > 0){
            userMeta = essayMatchService.findMatchUserMeta(userId, mock.getId());
        }
        int paperId = mock.getId().intValue();
        long startTime = mock.getStartTime().getTime();
        long endTime = mock.getEndTime().getTime();
        long currentTime = System.currentTimeMillis();
        Date reportDate = DateUtil.getMinutesBeforeDate(mock.getEndTime(), -(int) extraTime);
        long reportTime = reportDate.getTime();
        String enrollCountKey = RedisKeyConstant.getTotalEnrollCountKey(paperId);
        Integer enrollCount = (Integer) redisTemplate.opsForValue().get(enrollCountKey);
        Match match = Match.builder()
                //模考id
                .essayPaperId(paperId)
                //模考名称
                .name(mock.getName())
                //模考时间信息
                .timeInfo(getTimeInfo(startTime, endTime))
                //解析课信息
                .courseId(mock.getCourseId())
                .courseInfo(mock.getCourseInfo())
                //考试说明
                .instruction(mock.getInstruction())
                .instructionPC(mock.getInstructionPC())
                //暂时  4 2019申论考试 26 2020申论考试
                .tag(26)
                .essayStartTime(startTime)
                .essayEndTime(endTime)
                //1只有行测报告2只有申论报告3行测申论报告都有
                .flag(2)
                //报名人数(positionCount放的是总报名人数)
                .enrollCount(enrollCount == null ? 0 : enrollCount)
                /**
                 * add by zhaoxi (v7.1.12新需求，添加答题交卷时间限制)
                 */
                .enterLimitTime(enterLimitTime)
                .commitLimitTime(commitLimitTime)
                .build();


        //按钮状态的判断
        //已报名
        if (userMeta != null) {
            //答题卡id

            long answerId = userMeta.getPracticeId();

            //有答题卡，看过题
            if (answerId > 0) {
                //判断是否交卷
                String userAnswerStatusKey = RedisKeyConstant.getUserAnswerStatusKey(paperId);
                Object obj = redisTemplate.opsForHash().get(userAnswerStatusKey, userId + "");
                Integer answerCardStatus = Integer.parseInt(obj.toString());
                boolean isSubmit = (answerCardStatus.equals(EssayAnswerConstant.EssayAnswerBizStatusEnum.COMMIT.getBizStatus())
                        || answerCardStatus.equals(EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus()));
                //当前模考是否结束时
                boolean currentFinish = (currentTime >= endTime);
                boolean reportFinish = (currentTime >= reportTime);


                //已经交卷
                if (isSubmit) {
                    match.setStatus(reportFinish ? MatchStatus.REPORT_AVAILABLE : MatchStatus.REPORT_UNAVILABLE);
                } else if (!currentFinish) {
                    match.setStatus(MatchStatus.NOT_SUBMIT);
                } else {
                    match.setStatus(MatchStatus.REPORT_UNAVILABLE);
                }
                //没有看过题
            } else {
                if (startTime - currentTime >= TimeUnit.MINUTES.toMillis(enterLimitTime + startLimitTime)) {
                    //1。距开始大于一个小时
                    match.setStatus(MatchStatus.ENROLL);
                } else if (startTime - currentTime >= TimeUnit.MINUTES.toMillis(enterLimitTime)) {
                    //2。距开始小于一个小时,大于5分钟(11.04改成考前30分钟)
                    match.setStatus(MatchStatus.START_UNAVILABLE);
                } else if (currentTime - startTime < TimeUnit.MINUTES.toMillis(30) &&
                        currentTime < endTime) {
                    //3。距开始小于5分钟,且距离开始后小于30分钟
                    match.setStatus(MatchStatus.START_AVILABLE);
                } else {
                    //4。已经开始30分钟
                    match.setStatus(MatchStatus.MATCH_UNAVILABLE);
                }
            }

        } else {
            //未报名
            match.setStatus(MatchStatus.UN_ENROLL);
            //已经开始30分钟或者考试已结束
            if (currentTime - startTime >= TimeUnit.MINUTES.toMillis(30)
                    || currentTime > endTime) {
                //状态置为“未报名且错过报名”
                match.setStatus(MatchStatus.PASS_UP_ENROLL);

            }
        }
        //填充用户报名数据
        match.setUserMeta(userMeta);

        return match;
    }


    /**
     * 处理时间（将开始，结束的时间戳拼接成指定格式的字符串）
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public static String getTimeInfo(long startTime, long endTime) {
        Calendar instance = Calendar.getInstance();
        instance.setTime(new Date(startTime));

        int day = instance.get(Calendar.DAY_OF_WEEK);

        //考试时间：2017年8月20日（周日）09:00-11:00
        String timeInfo = DateFormatUtils.format(startTime, "yyyy年M月d日") + "（%s）%s-%s";
        String dayString = "";
        switch (day) {
            case Calendar.SUNDAY:
                dayString = "周日";
                break;

            case Calendar.MONDAY:
                dayString = "周一";
                break;

            case Calendar.TUESDAY:
                dayString = "周二";
                break;
            case Calendar.WEDNESDAY:
                dayString = "周三";
                break;
            case Calendar.THURSDAY:
                dayString = "周四";
                break;
            case Calendar.FRIDAY:
                dayString = "周五";
                break;

            case Calendar.SATURDAY:
                dayString = "周六";
                break;
        }

        timeInfo = String.format(timeInfo, dayString, DateFormatUtils.format(startTime, "HH:mm"),
                DateFormatUtils.format(endTime, "HH:mm"));

        return "考试时间：" + timeInfo;
    }




}
