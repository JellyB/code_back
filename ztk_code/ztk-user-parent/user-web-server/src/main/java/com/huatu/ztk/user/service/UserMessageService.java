package com.huatu.ztk.user.service;

import com.huatu.ztk.commons.CatgoryType;
import com.huatu.ztk.commons.PageBean;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.user.bean.UserDto;
import com.huatu.ztk.user.bean.UserMessage;
import com.huatu.ztk.user.common.MsgReadStatus;
import com.huatu.ztk.user.common.UserRedisKeys;
import com.huatu.ztk.user.dao.MobileUserDao;
import com.huatu.ztk.user.dao.SystemMessageDao;
import com.huatu.ztk.user.dao.UserMessageDao;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by shaojieyue
 * Created time 2016-06-16 20:21
 */

@Service
public class UserMessageService {
    private static final Logger logger = LoggerFactory.getLogger(UserMessageService.class);

    @Autowired
    private UserMessageDao userMessageDao;

    @Autowired
    private SystemMessageDao systemMessageDao;

    @Autowired
    private MobileUserDao mobileUserDao;

    @Resource(name = "redisTemplate")
    private RedisTemplate<String,String> redisTemplate;

    public PageBean<UserMessage> findMessage(long userId, long cursor, int size, int catgory) {
        //TODO 此处应该用redis存贮每个人的消息列表id,各种消息合并到一个表里面
        //TODO 后期需要优化
        cursor = Math.max(cursor, 0);

        //系统消息,区分科目
        List<UserMessage> systemMessageList = systemMessageDao.find(cursor, size, catgory);
        long newcursor = cursor + systemMessageList.size();

        // 第一次读取用户反馈的回复消息列表前5个
        if (cursor == 0 && catgory == CatgoryType.GONG_WU_YUAN) {
            List<UserMessage> mList = userMessageDao.getUserFeedBackMsg(userId);
            systemMessageList.addAll(mList);
        }

        //排序
        systemMessageList.sort((UserMessage o1, UserMessage o2) -> (o2.getCreateTime() + "").compareTo(o1.getCreateTime() + ""));

        String setKey = UserRedisKeys.getUserMsgSetKey(userId, catgory);
        Set<String> members = redisTemplate.opsForSet().members(setKey);

        for (UserMessage message : systemMessageList) {
            message.setStatus(!members.contains(message.getId() + "") ?
                    MsgReadStatus.NEW_MSG : MsgReadStatus.OLD_MSG);
        }

        //消息数暂时写死
        PageBean pageBean = new PageBean(systemMessageList, newcursor, -1);
        return pageBean;
    }

    /**
     * 查询单个信息详情
     * @param userId
     * @param mid
     * @return
     * @throws BizException
     */
    public UserMessage findById(long userId, long mid) throws BizException {
        //先查询系统信息
        UserMessage userMessage = systemMessageDao.findById(mid);
        if (userMessage != null) {//存在则直接返回
            return userMessage;
        }

        //查询用户个人信息
        userMessage = userMessageDao.findById(mid);
        if (userMessage != null) {
            //获取移动端用户信息
            UserDto userDto = mobileUserDao.getUserByPcId(userId);
            if (userDto != null && userMessage.getUid()!= userDto.getId()) {//个人消息不属于该用户
                throw new BizException(CommonErrors.PERMISSION_DENIED);
            }
            return userMessage;
        }

        userMessage = userMessageDao.findUserFeedBackMsgById(mid);
        if (userMessage != null) {
            if (userMessage.getUid()!=userId) {//个人消息不属于该用户
                throw new BizException(CommonErrors.PERMISSION_DENIED);
            }
        }
        return userMessage;
    }

    /**
     * 添加到已看过的msgid set
     * @param userId
     * @param mid
     * @param catgory
     */
    public void addMsgReadSet(long userId, long mid, int catgory) {
        String setKey = UserRedisKeys.getUserMsgSetKey(userId, catgory);
        redisTemplate.opsForSet().add(setKey, mid + "");
    }

    /**
     * 获得未读消息个数
     * @param userId
     * @param catgory
     * @return
     * @throws BizException
     */
    public int getUnReadMsgCount(long userId,int catgory) throws BizException {
        String setKey = UserRedisKeys.getUserMsgSetKey(userId, catgory);


        Set<String> members = redisTemplate.opsForSet().members(setKey);

        List<UserMessage> messages = systemMessageDao.findAll(catgory);

        //只有公务员的有反馈回复消息
        if (catgory == CatgoryType.GONG_WU_YUAN) {
            List<UserMessage> userMessageList = userMessageDao.getUserFeedBackMsg(userId);
            messages.addAll(userMessageList);
        }

        return (int) messages.stream().filter(m -> !members.contains(m.getId() + "")).count();
    }

    public PageBean<UserMessage> findMessageV2(long userId, long cursor, int size, int catgory) {
        //第一次查询时查询系统消息
        List<UserMessage> systemMessageList = new LinkedList<>();
        if(0 == cursor){
            //系统消息,区分科目
            systemMessageList = systemMessageDao.find(cursor, size, catgory);

            if(CollectionUtils.isNotEmpty(systemMessageList)){
                size = size - systemMessageList.size();
            }
        }
        cursor = Math.max(cursor, 0);
        //控制负数
        size = Math.max(size, 0);

        // 第一次读取用户反馈的回复消息列表前5个
        if (catgory == CatgoryType.GONG_WU_YUAN) {
            List<UserMessage> mList = userMessageDao.getUserFeedBackMsgV2(userId,cursor,size);
            systemMessageList.addAll(mList);
        }

        //排序
        systemMessageList.sort((UserMessage o1, UserMessage o2) -> (o2.getCreateTime() + "").compareTo(o1.getCreateTime() + ""));

        String setKey = UserRedisKeys.getUserMsgSetKey(userId, catgory);
        Set<String> members = redisTemplate.opsForSet().members(setKey);

        for (UserMessage message : systemMessageList) {
            message.setStatus(!members.contains(message.getId() + "") ?
                    MsgReadStatus.NEW_MSG : MsgReadStatus.OLD_MSG);
        }

        //消息数暂时写死
        PageBean pageBean = new PageBean(systemMessageList, cursor+systemMessageList.size(), -1);
        return pageBean;
    }

}
