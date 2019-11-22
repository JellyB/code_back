package com.huatu.tiku.essay.task;

import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.label.LabelRedisKeyConstant;
import com.huatu.tiku.essay.service.EssayLabelService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

/**
 * 未完成批注 超时关闭
 * @author zhaoxi
 */
@Component
@Slf4j
public class LabelCloseTask extends TaskService{
    private static final Logger logger = LoggerFactory.getLogger(LabelCloseTask.class);

    @Autowired
    private EssayLabelService essayLabelService;

    private static final long TASK_LOCK_EXPIRE_TIME = 2;
    @PostConstruct
    public void init() {
        //添加停止任务线程
        Runtime.getRuntime().addShutdownHook(new Thread(()-> unlock()));
    }

    @Override
    public void run() {
        //查询超时&&未关闭订单
        String   serverIp = getLocalLock();
        logger.info("auto close unfinished label task start.server={}", serverIp);
        //关闭未完成批注
        essayLabelService.closeUnfinishedLabel();
    }

    @Scheduled(fixedRate = 60000)
    public void labelClose() throws BizException {
        task();
    }



    @Override
    public String getCacheKey() {
        return LabelRedisKeyConstant.getLabelCloseLockKey();
    }



    @Override
    protected long getExpireTime() {
        return TASK_LOCK_EXPIRE_TIME;
    }
}
