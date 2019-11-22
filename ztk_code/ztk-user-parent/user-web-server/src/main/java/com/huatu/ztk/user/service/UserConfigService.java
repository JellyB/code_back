package com.huatu.ztk.user.service;

import java.util.HashMap;
import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.huatu.common.consts.SensorsEventEnum;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.knowledge.api.SubjectDubboService;
import com.huatu.ztk.user.bean.UserConfig;
import com.huatu.ztk.user.common.UserRedisKeys;
import com.huatu.ztk.user.dao.UserConfigDao;
import com.sensorsdata.analytics.javasdk.SensorsAnalytics;

/**
 * Created by shaojieyue
 * Created time 2016-12-20 11:16
 */
@Service
public class UserConfigService {
    private static final Logger logger = LoggerFactory.getLogger(UserConfigService.class);
    @Autowired
    private UserConfigDao userConfigDao;

    @Autowired
    private UserService userService;

    @Autowired
    private SubjectDubboService subjectDubboService;

    @Autowired
    private SensorsAnalytics sensorsAnalytics;


    @Resource(name = "redisTemplate")
    private RedisTemplate<String, String> redisTemplate;

    public UserConfig getDefaultUserConfig(int catgory) {
        return UserConfig.builder()
                .area(-9)
                .subject(getDefaultSubject(catgory))
                .category(catgory)
                .qcount(10)
                .errorQcount(10)
                .build();
    }

    /**
     * 获取科目下的默认知识点类目
     *
     * @param catgory
     * @return
     */
    private int getDefaultSubject(int catgory) {
        int subject = -1;
        switch (catgory) {
            case 1:
                subject = 1;
                break;
            case 3:
                subject = 2;
                break;
            case 200100045:
                subject = 100100262;
                break;
            case 200100047:
                subject = 100100175;
                break;
            case 41:
                subject = 100100267;
                break;
            case 42:
                subject = 100100268;
                break;
            case 43:
                subject = 100100263;
                break;
            case 200100000:
                subject = 410;
                break;
            case 200100002:
                subject = 420;
                break;
            case 100100633:
                subject = 100100634;
                break;
            case 200100046:
                subject = 430;
                break;
        }
        return subject;
    }

    /**
     * 更新用户基础配置，分别保存到redis、mongoDb中
     *
     * @param area        所属区域
     * @param qcount      试题数量
     * @param subject     知识点类目
     * @param uid         用户id
     * @param errorQcount
     * @return
     */
    public UserConfig update(int area, int qcount, int subject, long uid, int catgory, int errorQcount,int terminal, String ucid, String uname) throws BizException {
        //mongo中查询用户配置，mongo为空，设置
        UserConfig userConfig = findByUidAndCatgory(uid, catgory);

        if (userConfig == null) {
            userConfig = getDefaultUserConfig(catgory);
            userConfig.setId(getConfigId(uid, catgory));
            userConfig.setUid(uid);
        }
        if (qcount > 0 && qcount <= 30) {
            userConfig.setQcount(qcount);
        } else if (qcount > 30) {
            userConfig.setQcount(30);
        }
        if (area > 0 || area == -9) {
            userConfig.setArea(area);
        }
        if (subject > 0) {
            userConfig.setSubject(subject);
        }

        if (errorQcount > 0) {
            userConfig.setErrorQcount(errorQcount);
        }

        userConfig.setCategory(catgory);
        /**
         * 学员修改信息数据上报
         */
        reportUserConfig2Sensors(uid, uname, catgory);

        userService.updateUserConfigSession(userConfig,terminal);//将session保存到redis中
        save(userConfig);

        return userConfig;
    }
    
    /**
     * TODO 异步实现需要移出改Service
     * 上报考试类型
     * @param ucid
     * @param uname
     * @param catgory
     */
    public void reportUserConfig2Sensors(long ucid, String uname, int catgory) {
    	try{
            String categoryName = subjectDubboService.getCategoryNameById(catgory);
            //logger.info("用户修改考试类型:{}, 新的对应的考试类型为:{}", uname, categoryName);
            HashMap<String, Object> saProperties = Maps.newHashMap();
            saProperties.put(SensorsEventEnum.EXAM_CATEGORY_AREA.getCode(), categoryName);
            sensorsAnalytics.profileSet(ucid + "", true, saProperties);
            sensorsAnalytics.flush();
        }catch (Exception e){
            logger.error("sa track error:" + e);
        }
    }


    /**
     * 根据uid，查询是否有该用户
     *
     * @param uid 用户id
     * @return
     */
    public UserConfig findByUidAndCatgory(long uid, int catgory) {
        return userConfigDao.findById(getConfigId(uid, catgory));
    }

    /**
     * 插入用户基础配置
     *
     * @param config 用户基础配置
     */
    public void save(UserConfig config) {
        logger.debug("save config={}", JsonUtil.toJson(config));
        userConfigDao.save(config);
    }

    public String getConfigId(long uid, int catgroy) {
        return uid + "_" + catgroy;
    }

    /**
     * zset添加配置id
     *
     * @param userConfig
     */
    public void addLastConfigId(UserConfig userConfig) {
        long userId = userConfig.getUid();
        String setKey = UserRedisKeys.getLastConfigIdSet(userId);

        ZSetOperations<String, String> opsForZSet = redisTemplate.opsForZSet();

        opsForZSet.add(setKey, userConfig.getId(), System.currentTimeMillis());
    }


    /**
     * 获得最近一个用户配置
     *
     * @param userId
     * @return
     */
    public UserConfig getLastConfig(long userId) {
        String setKey = UserRedisKeys.getLastConfigIdSet(userId);

        ZSetOperations<String, String> opsForZSet = redisTemplate.opsForZSet();

        Set<String> configIds = opsForZSet.reverseRange(setKey, 0, 0);

        if (configIds.size() > 0) {
            String id = configIds.iterator().next();
            return userConfigDao.findById(id);
        }
        return null;
    }
}
