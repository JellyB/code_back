package com.huatu.ztk.user.galaxy;

import com.huatu.ztk.user.bean.UserDto;
import org.apache.log4j.Logger;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;


/**
 * @author jbzm
 * @date Create on 2018/3/19 16:42
 */
@Async
@Component
public class SendRegister {
    private static final Logger logger = Logger.getLogger(SendRegister.class);
    /**
     * Token容器
     */
    private Set<UserRegister> userDtos = new HashSet<>();
    /**
     * 最大时间间隔:ms
     */
    private static final long INTERVAL = 60000L;
    /**
     * 缓存长度  千万不要乱改
     */
    private static final int CACHE_LENGTH = 200;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 按量发送
     *
     * @param userDto UserDto
     */
    public void send(UserDto userDto, String regIp,int terminal) {
        try {
            if (userDto == null) {
                return;
            } else {
                UserRegister userRegister = new UserRegister(userDto.getId()
                        , userDto.getName()
                        , userDto.getRegFrom(), userDto.getCreateTime(), regIp,terminal);
                userDtos.add(userRegister);
            }
            if (userDtos.size() > CACHE_LENGTH) {
                sendToMq();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 定时发送
     */
    @Scheduled(fixedRate = INTERVAL)
    public void timeSend() {
        try {
            if (userDtos.size() > 0) {
                sendToMq();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendToMq() {
        if (userDtos.size() > 0) {
            Set<UserRegister> tempTokens = new HashSet<>(userDtos);
            userDtos.clear();
            rabbitTemplate.convertAndSend("galaxy_data_topic", "com.galaxy.register", tempTokens);
            logger.info("数据上报:" + tempTokens.size());
        }
    }
}
