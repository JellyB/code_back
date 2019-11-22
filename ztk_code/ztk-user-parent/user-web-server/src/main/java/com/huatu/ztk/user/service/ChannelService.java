package com.huatu.ztk.user.service;


import com.huatu.common.consts.TerminalType;
import com.huatu.ztk.user.bean.AppChannel;
import com.huatu.ztk.user.common.CourseSourceType;
import com.huatu.ztk.user.common.DeviceTokenState;
import com.huatu.ztk.user.dao.ChannelDao;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author zhouwei
 * @Description:
 * @create 2018-06-07 上午11:55
 **/
@Service
public class ChannelService {
    @Resource
    ChannelDao channelDao;

    @Autowired
    RedisTemplate redisTemplate;

    private org.slf4j.Logger logger = LoggerFactory.getLogger(ChannelService.class);

    /**
     * 添加deviceToken
     *
     * @Description 根据是否存在状态, 判断执行操作。
     * 1,deviceToken已经存在并且激活，不执行任何操作；
     * 2,deviceToken不存在，入库；
     * 3,deviceToken已经存在，但是尚未激活状态，执行更新激活操作，激活成功，给第三方渠道激活回调
     */
    @Async
    public void addChannel(String deviceToken, String ip, long gmtCreate, String version, String model, String cv,int isBreakPrison) throws Exception {
        AppChannel channel = channelDao.selectSourceChannel(deviceToken, false);
        if (null == channel) {
            channelDao.insertChannel(deviceToken, ip, gmtCreate, DeviceTokenState.ACTIVE_STATE,
                    0, CourseSourceType.IOS, version, model, cv, false,isBreakPrison);
        } else {
            if (channel.getState() == DeviceTokenState.UN_ACTIVE_STATE) {
                int count = channelDao.updateChannelSateByToken(deviceToken, version, model, ip, cv);
                if (count > 0) {
                    callBackUrl(channel.getCallBack());
                }
            }
        }

    }

    /**
     * 多渠道排重
     *
     * @Description 1.deviceToken数据库中不存在, 直接插入库中, 返回(deviceToken, 0)
     * 2.deviceToken已存在，如果是激活状态，返回(deviceToken,1);未激活状态,如果是当天的第一次来，返回(deviceToken,0)
     * 如果当天再次排重,返回（deviceToken,1）是的
     */
    public Map<String, Integer> checkChannel(String deviceToken, String ip, long gmtCreate, int source, Boolean isCool) throws ParseException {
        //SetOperations setOperations = redisTemplate.opsForSet();
        HashMap map = new HashMap();
        AppChannel deviceTokenResult = channelDao.selectSourceChannel(deviceToken, false);
        if (null != deviceTokenResult) {
            int state = deviceTokenResult.getState();
            if (state == DeviceTokenState.ACTIVE_STATE) {
                map.put(deviceToken, DeviceTokenState.REPEAT);
            } else if (state == DeviceTokenState.UN_ACTIVE_STATE) {
                if (null != deviceTokenResult.getUpdateTime()) {
                    if (isSameDay(deviceTokenResult.getUpdateTime().toString())) {
                        map.put(deviceToken, DeviceTokenState.REPEAT);
                    } else {
                        map.put(deviceToken, DeviceTokenState.NO_REPEAT);
                    }
                    AppChannel appChannel = new AppChannel();
                    appChannel.setDeviceToken(deviceToken);
                    channelDao.updateChannelByParams(appChannel);
                }
            } else {
                //其他情况拒绝
                map.put(deviceToken, DeviceTokenState.REPEAT);
            }
        } else {
            //放入缓存
            //setOperations.add(SourceType.deviceTokenKey(deviceToken), deviceToken);
            channelDao.insertChannel(deviceToken, ip, gmtCreate, DeviceTokenState.UN_ACTIVE_STATE, source, CourseSourceType.IOS, "", "", "", isCool,DeviceTokenState.IS_BREAK_PRISON);
            map.put(deviceToken, DeviceTokenState.NO_REPEAT);
        }
        return map;
    }

    /**
     * 保存callBack参数
     *
     * @param tokens
     * @return 2 已经激活 1 未激活
     */
    public Object saveCallBack(String tokens, int source, String callBack) {
        //异步还没有走完,保存不了参数
        AppChannel appChannel = new AppChannel();
        appChannel.setDeviceToken(tokens);
        appChannel.setSource(source);
        appChannel.setCallBack(callBack);
        return channelDao.updateChannelByParams(appChannel);

    }

    /**
     * 回调访问
     *
     * @param
     * @return
     * @throws Exception
     */
    public void callBackUrl(String strUrl) {
        RestTemplate restTemplate = new RestTemplate();
        if (StringUtils.isNotEmpty(strUrl)) {
            ResponseEntity<String> responseMsg = restTemplate.getForEntity(strUrl.trim(), String.class);
            if (null != responseMsg) {
                if (!responseMsg.getStatusCode().equals(200)) {
                    logger.info("callBack result is :{}", responseMsg);
                }
            }
        }
    }


    public Boolean isSameDay(String updateTime) {
        //未激活,跟当前时间比较,每天第一次,返回 (deviceToken:0)
        // 第一天的其他次数，返回（deviceToken:1）
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date now = df.parse(df.format(new Date()));
            Date dateConvert = df.parse(updateTime);
            Date updateTimeStr = df.parse(df.format(dateConvert));
            if (updateTimeStr.compareTo(now) == 0) {
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Async
    public void insertChannel(String deviceToken, String ip, long gmtCreate, int source, boolean isCool,
                              String version, String model, String cv,int terminal) {
        AppChannel appChannel = channelDao.selectSourceChannel(deviceToken, isCool);
        if (null == appChannel) {
            if (isCool) {
                int sourceType=CourseSourceType.IOS;
                if (terminal==TerminalType.ANDROID || terminal==TerminalType.ANDROID_IPAD){
                    sourceType=CourseSourceType.ANDROID;
                }
                channelDao.insertChannel(deviceToken, ip, gmtCreate, DeviceTokenState.ACTIVE_STATE, source, sourceType, version, model, cv, isCool,DeviceTokenState.IS_BREAK_PRISON);
            } else {
                channelDao.insertChannel(deviceToken, ip, gmtCreate, DeviceTokenState.ACTIVE_STATE, source, CourseSourceType.ANDROID, "", "", "", isCool,DeviceTokenState.IS_BREAK_PRISON);
            }
        }
    }

}
