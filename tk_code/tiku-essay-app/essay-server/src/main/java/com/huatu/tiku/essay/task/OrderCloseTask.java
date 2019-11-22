package com.huatu.tiku.essay.task;

import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.cache.RedisKeyConstant;
import com.huatu.tiku.essay.service.UserCorrectGoodsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
 * 申论订单超时关闭
 *
 * @author zhaoxi
 */
@Component
@Slf4j
public class OrderCloseTask extends TaskService{
    //    private static final log log = logFactory.getlog(OrderCloseTask.class);
    @Autowired
    UserCorrectGoodsService userCorrectGoodsService;

    private static final long TASK_LOCK_EXPIRE_TIME = 1;
    @Override
    public void run() {
        //查询超时&&未关闭订单
        String serverIp = getLocalLock();
        log.info("auto close order task start.server={}", serverIp);
        userCorrectGoodsService.closeOrder();
    }

    @Scheduled(fixedRate = 60000)
    public void orderClose() throws BizException {
       task();
    }



    @Override
    public String getCacheKey() {
        return RedisKeyConstant.getOrderCloseLockKey();
    }

    @Override
    protected long getExpireTime() {
        return TASK_LOCK_EXPIRE_TIME;
    }
}
