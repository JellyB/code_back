package com.huatu.ztk.user.service;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.user.bean.Activity;
import com.huatu.ztk.user.bean.FreeCourseBean;
import com.huatu.ztk.user.common.*;
import com.huatu.ztk.user.dao.ActivityDao;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 活动Service
 */
@Service
public class ActivityService {
    private static final Logger logger = LoggerFactory.getLogger(ActivityService.class);

    @Autowired
    private ActivityDao activityDao;

    @Resource(name = "redisTemplate")
    private RedisTemplate<String,String> redisTemplate;

    private static final int WINDOW_OFF = 0;
    private static final int WINDOW_ON = 1;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 查询活动列表
     * @return
     */
    public List<Activity> queryMobileActivitys(){
        List<Activity> tmp = activityDao.queryMobileactivity();
        List<Activity> activity = new ArrayList<>();

        //将活动时间与系统时间比较
        long currentTime = System.currentTimeMillis();

        List<Activity> notTimeList = new ArrayList<>();
        List<Activity> onlineList = new ArrayList<>();
        List<Activity> offlineList = new ArrayList<>();
        for (Activity act : tmp) {
            if (act.getBeginTime() > currentTime) {
                act.setStatus(ActivityStatus.NOT_TIME);
                notTimeList.add(act);
            } else if (currentTime <= act.getEndTime()) {
                act.setStatus(ActivityStatus.ONLINE);
                onlineList.add(act);
            } else {
                act.setStatus(ActivityStatus.OFFLINE);
                offlineList.add(act);
            }
        }

        //按上线，未上线，下线的顺序排列
        activity.addAll(onlineList);
        activity.addAll(notTimeList);
        activity.addAll(offlineList);

        return activity;
    }

    /**
     * 根据id查询活动对象
     * @param id
     * @return
     */
    public Activity findById(long id) {
        return activityDao.findById(id);
    }

    /**
     *获得用户活动未读个数
     * @param userId
     * @return
     * @throws BizException
     */
    public int getUnReadActCount(long userId) throws BizException {
        int count = 0;

        //取出用户查看活动列表时间
        String key = String.format(UserRedisKeys.USER_ACT_READ, userId);
        String ret = redisTemplate.opsForValue().get(key);

        if (ret != null) {
            //时间格式转换
            long readTime = Long.parseLong(ret);
            String readTimeString = DateFormatUtils.format(readTime,"yyyy-MM-dd HH:mm:ss");
            count = activityDao.getUnReadActCount(readTimeString);
        } else {
            //缓存为空，说明用户从未打开活动中心，返回总的活动个数
            count = activityDao.queryMobileactivity().size();
        }
        return count;
    }

    /**
     * 点击量增加
     * @param aid
     * @throws BizException
     */
    public void pvadd(long aid) throws BizException {
        activityDao.pvadd(aid);
    }


    /**
     *好评送课
     * @param userId
     * @param username
     * @param terminal
     * @param cv   @return
     * @param catgory
     * @throws BizException
     */
    public String sendCommentCourse(long userId, String username, int terminal, String cv, int catgory) throws BizException {

        sendCourse(username, terminal, CourseType.COMMENT,catgory);

        //设置送课状态
        String markKey = CourseRedisKey.getCommentCourseSendMarkKey(userId, cv);
        //3个月过期
        redisTemplate.opsForValue().set(markKey, "1", 90, TimeUnit.DAYS);

        String courseName = redisTemplate.opsForValue().get(CourseRedisKey.COMMENT_COURSE_NAME);
        return StringUtils.trimToEmpty(courseName);
    }


    /**
     * 赠送课程
     * @param username ucenter用户名
     * @param terminal 终端类型
     * @param courseType 赠送课程类型
     */
    public void sendCourse(String username,int terminal, int courseType,int catgory) {
        FreeCourseBean bean = FreeCourseBean.builder()
                .username(username)
                .tag(courseType)
                .catgory(catgory)
                .source(CourseSourceType.getSourceTypeByTerminal(terminal))
                .build();
        rabbitTemplate.convertAndSend("","send_free_course_queue",bean);

        logger.info("send course bean={}", JsonUtil.toJson(bean));
    }

    /**
     * 送课弹窗开关，0：关闭，1：开启
     * @param userId
     * @param cv
     * @return
     */
    public int getFreeCourseStatus(long userId, String cv) {
        if (userId < 1) {//用户处于未登录状态,则不弹出送课提示
            return WINDOW_OFF;
        }

        String openStatusStr = redisTemplate.opsForValue().get(CourseRedisKey.COMMENT_COURSE_SWITCH);

        if (StringUtils.isBlank(openStatusStr)) {
            openStatusStr = "0";
        }

        int openStatus = Integer.parseInt(openStatusStr);

        String markKey = CourseRedisKey.getCommentCourseSendMarkKey(userId, cv);

        //开关开启，且没有送过，弹窗
        if (openStatus > 0 && !redisTemplate.hasKey(markKey)) {
            return WINDOW_ON;
        }

        return WINDOW_OFF;
    }
}
