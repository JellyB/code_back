package com.huatu.ztk.user.utils;

import com.google.common.collect.Lists;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.TerminalType;
import com.huatu.ztk.user.bean.Message;
import com.huatu.ztk.user.dao.AdvertMessageDao;
import com.huatu.ztk.user.service.AdvertMessageService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhouwei
 * @Description: 如果是IOS新版本，轮播图模考大赛应该是estimatePaper/home  替换
 * @create 2017-12-22 下午2:00
 **/
public class MessageUtil {
    public static void dealMessage(List<Message> messages) {
        for (Iterator<Message> i = messages.iterator(); i.hasNext(); ) {
            Message message = i.next();
            //模考大赛IOS
            if (message != null && message.getTarget() != null && message.getTarget().contains("match/detail")) {
                message.setTarget("ztk://estimatePaper/home");
                break;
                //精准估分轮播图IOS端不展示
            } else if (message != null && message.getTarget() != null && message.getTarget().contains("estimatePaper/home")) {
                i.remove();
                break;
            }

        }
    }

    /**
     * 轮播图白名单
     */
    public static void filterMessage(RedisTemplate<String, String> redisTemplate, long uid, AdvertMessageDao advertMessageDao, AdvertMessageService advertMessageService, List<Message> result) {
        final ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String auditListRedis = valueOperations.get("picList");
        if (StringUtils.isNotBlank(auditListRedis) && auditListRedis.contains(uid + "")) {
            String tempIds = valueOperations.get("tempId");
            List<HashMap<String, Object>> messageMaps = advertMessageDao.findAdvert(tempIds);
            if (null == result) {
                result = Lists.newArrayList();
            }
            if (CollectionUtils.isNotEmpty(messageMaps)) {
                for (HashMap<String, Object> messageMap : messageMaps) {
                    Message message = advertMessageService.convertToMessage(messageMap);
                    result.add(message);
                }
            }
        }
    }

    /**
     * 轮播图白名单
     */
    public static List<Message> filterWhiteUserName(RedisTemplate<String, String> redisTemplate, AdvertMessageDao advertMessageDao, AdvertMessageService advertMessageService, List<Message> result) {
    	List<Message> resultNew =Lists.newArrayList();
    	resultNew.addAll(result);
    	final ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String tempIds = valueOperations.get("tempId");
        if (StringUtils.isEmpty(tempIds)) {
            return null;
        }

        List<HashMap<String, Object>> messageMaps = advertMessageDao.findAdvert(tempIds);
        if (null == result) {
            result = Lists.newArrayList();
        }
        if (CollectionUtils.isNotEmpty(messageMaps)) {
            for (HashMap<String, Object> messageMap : messageMaps) {
                Message message = advertMessageService.convertToMessage(messageMap);
                resultNew.add(message);
            }
        }
        return resultNew;
    }


    public static boolean isIosNewVersion(int terminal, String userCv, String newCv) {
        return (terminal == TerminalType.IPHONE || terminal == TerminalType.IPHONE_IPAD)
                && userCv.compareTo(newCv) >= 0;
    }

    public static void filterEssayForOldVersion(List<Message> result) {
        if (result != null && result.size() > 0) {

            for (Iterator<Message> i = result.iterator(); i.hasNext(); ) {
                Message m = i.next();
                if (m != null && m.getTarget() != null && m.getTarget().indexOf("essay") > 0) {
                    i.remove();
                }
            }
        }
    }


//
//    public static void main(String[] args) {
//        List<Message> messages = new ArrayList<>();
//        Message m = new Message();
//        m.setTarget("ztk://estimatePaper/home");
//        m.setTarget("ztk://match/detail");
//        messages.add(m);
//        dealMessage(messages);
//       System.out.println(messages.size());
//    }
}
