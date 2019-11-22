package com.huatu.ztk.user.service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.user.bean.RewardMessage;
import com.huatu.ztk.user.bean.UserDto;
import com.huatu.ztk.user.bean.UserSign;
import com.huatu.ztk.user.common.UserErrors;
import com.huatu.ztk.user.bean.SimpleUserDto;
import com.huatu.ztk.user.dao.UserDao;
import com.huatu.ztk.user.dao.UserSignDao;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.huatu.ztk.commons.RewardConstants.*;

/**
 * Created by linkang on 2017/10/14 下午2:46
 */
@Service
public class UserRewardService {

    private static final Logger logger = LoggerFactory.getLogger(UserRewardService.class);

    public static final int DEFAULT_TYPE = 1;

    private final String NOTICE_PUSH_UNAME_PUKEY = "notice.push.uname.pukey";

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserSignDao userSignDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private UcenterService ucenterService;
    @Autowired
    private UserService userService;
    /**
     * 签到
     * @param userId
     * @param uname
     * @param signDay 签到日期
     * @return
     * @throws BizException
     */
    public RewardMessage sign(long userId, String uname, Date signDay) throws BizException {
    	 long start4 = System.currentTimeMillis();
        UserSign sign = userSignDao.findByUidAndDate(userId, signDay);
        if (sign != null) {
            throw new BizException(UserErrors.SIGN_EXISTS);
        }
        long start5 = System.currentTimeMillis();
        logger.info("zc findByUidAndDate spend:{}",start5-start4);
        Date last = DateUtils.addDays(signDay, -1);
        UserSign lastSign = userSignDao.findByUidAndDate(userId, last);
        long start6 = System.currentTimeMillis();
        logger.info("zc findByUidAndDate spend:{}",start6-start5);
        int num = (lastSign != null) ?
                lastSign.getNumber() + 1 : 1;

        UserSign userSign = UserSign.builder()
                .type(DEFAULT_TYPE)
                .uid(userId)
                .signTime(signDay)
                .number(num)
                .build();
        userSignDao.insert(userSign);
        long start7 = System.currentTimeMillis();
        logger.info("zc insert userSignDao spend:{}",start7-start6);
        //满三十天，额外+50
        int score = (num % 30 == 0) ? 50 + Math.min(5, num) : Math.min(5, num);

        RewardMessage message = RewardMessage.builder()
                .uname(uname)
                .uid(userId)
                .action(ACTION_ATTENDANCE)
                .bizId(userId + "_" + DateFormatUtils.format(signDay, "yyyyMMdd"))
                .experience(score)
                .timestamp(System.currentTimeMillis())
                .gold(score)
                .build();
        rabbitTemplate.convertAndSend("", MQ_NAME, message);
        long start8 = System.currentTimeMillis();
        logger.info("zc convertAndSend spend:{}",start8-start7);
        logger.info("send msg={}", JsonUtil.toJson(message));

        return message;
    }

    /**
     * 发送注册加积分消息
     *
     * @param userId
     * @param uname
     */
    public void sendRegisterMessage(long userId, String uname) {
        RewardMessage message = RewardMessage.builder()
                .uname(uname)
                .timestamp(System.currentTimeMillis())
                .uid(userId)
                .action(ACTION_REGSITER)
                .bizId(userId + "_" + "register")
                .build();
        rabbitTemplate.convertAndSend("", MQ_NAME, message);
        logger.info("send msg={}", JsonUtil.toJson(message));
    }

    /**
     * 查询当天的签到
     *
     * @param userId
     * @return
     * @throws BizException
     */
    public UserSign findTodaySign(long userId) throws BizException {
        UserSign sign = userSignDao.findByUidAndDate(userId, new Date());
        if (sign == null) {
            throw new BizException(UserErrors.NOT_SIGN);
        }

        return sign;
    }

    public long getUIdByUserName(String userName)  throws BizException {
        long userId = 0;
        UserDto userDto = userDao.findByName(userName);
        if(null == userDto){
            throw new BizException(UserErrors.USER_NOT_EXIST);
        }else{
            userId =  userDto.getId();
        }

        logger.info(""+userId);
        return userId;
    }

    public String getUserNameById(Integer userId)  throws BizException {
        try{
            UserDto userDto = userDao.findByUserId(String.valueOf(userId));
            if(null == userDto){
                throw new BizException(UserErrors.USER_NOT_EXIST);
            }else{
                return userDto.getName();
            }
        }catch (Exception e){
            logger.error("getUserNameById exception, userId:{}, error:{}", userId, e.getMessage());
            return "";
        }

    }

    /**
     * 批量获取用户简单信息
     * @param userNames
     * @return
     */
    public List<JSONObject> getSimpleUserInfoBatch(List<String> userNames) {
        final List<JSONObject> jsonObjects = Lists.newArrayList();
        if (CollectionUtils.isEmpty(userNames)) {
            return Lists.newArrayList();
        }
        HashOperations hashOperations = redisTemplate.opsForHash();
        List<String> keyForMysql = Lists.newArrayList();
        Map<String,String> putAllMap = Maps.newHashMap();
        userNames.forEach(item->{
            if(hashOperations.hasKey(NOTICE_PUSH_UNAME_PUKEY, item)){
                String value = String.valueOf(hashOperations.get(NOTICE_PUSH_UNAME_PUKEY, item));
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("userName", item);
                jsonObject.put("userId", Long.valueOf(value));
                jsonObjects.add(jsonObject);
            }else{
                keyForMysql.add(item);
            }
        });

        if(CollectionUtils.isNotEmpty(keyForMysql)){
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("uNames", keyForMysql);
            logger.info("key select from mysql:{}", keyForMysql);
            List<SimpleUserDto> list = userDao.getUserInfoByUserNameBatch(parameters);
            if(CollectionUtils.isEmpty(list)){
                return jsonObjects;
            }
            list.forEach(item -> {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("userName", item.getUserName());
                jsonObject.put("userId", item.getUserId());
                jsonObjects.add(jsonObject);
                putAllMap.put(item.getUserName(), String.valueOf(item.getUserId()));
            });
        }
        if(putAllMap.size() > 0){
            hashOperations.putAll(NOTICE_PUSH_UNAME_PUKEY, putAllMap);
        }
        return jsonObjects;
    }
}
