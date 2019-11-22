package com.huatu.ztk.user.task;

import com.huatu.ztk.redisqueue.core.AbstractRedisQueueListener;
import com.huatu.ztk.user.bean.NicknameUpdateMessage;
import com.huatu.ztk.user.bean.UserDto;
import com.huatu.ztk.user.dao.UserDao;
import com.huatu.ztk.user.service.UserService;
import com.huatu.ztk.user.service.UserSessionService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author hanchao
 * @date 2017/11/1 9:39
 */
@Component
public class UserNicknameUpdateTask extends AbstractRedisQueueListener<NicknameUpdateMessage> {
    private static final Logger log = LoggerFactory.getLogger(UserNicknameUpdateTask.class);
    @Autowired
    private UserDao userDao;
    @Autowired
    private UserSessionService userSessionService;
    @Autowired
    private UserService userService;

    @Override
    public String queue() {
        return "queue.tiku_user_nick_update";
    }

    @Override
    public void consumeContent(NicknameUpdateMessage nicknameUpdateMessage) {
        log.info("update nick:{}",nicknameUpdateMessage);
        UserDto user = userDao.findByName(nicknameUpdateMessage.getUsername());
        if(user != null){
            //更新昵称
            if(StringUtils.isNotEmpty(nicknameUpdateMessage.getNickname())){
                userDao.modifyNickname(user.getId(),nicknameUpdateMessage.getNickname());
            }
            //更新头像
            if(StringUtils.isNotEmpty(nicknameUpdateMessage.getAvatar())){
                userDao.updateAvatar(user.getId(),nicknameUpdateMessage.getAvatar());
            }
            //目前对于session的修改无法同步到客户端，暂时忽略
//            String token = userSessionService.getTokenById(user.getId());
//            if(StringUtils.isNotBlank(token)){
//                //没有token的话只从数据库更新
//                userDao.modifyNickname(user.getId(),nicknameUpdateMessage.getNickname());
//            }else{
//                //有token
//            }
        }
    }
}
