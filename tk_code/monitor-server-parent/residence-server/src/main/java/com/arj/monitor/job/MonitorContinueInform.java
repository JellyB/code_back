package com.arj.monitor.job;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.arj.monitor.entity.InformRecord;
import com.arj.monitor.repository.InformRecordRepository;
import com.arj.monitor.repository.ServerInfoRepository;
import com.arj.monitor.service.impl.InformServiceImpl;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author zhangchong
 *
 */
@Slf4j
@Component
public class MonitorContinueInform {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ServerInfoRepository serverInfoRepository;
    @Autowired
    private RedisTemplate redisTemplate;


    @Resource(name="informServiceImpl")
    private InformServiceImpl informServiceImpl;
    @Autowired
    private InformRecordRepository informRecordRepository;
    private static final int SUCCESS_CODE = 1000000;
    private static final String SUCCESS_CODE_KEY = "code";
    private static final String SUCCESS_CODE_DATA = "data";
    private static final String SUCCESS_CODE_UP = "up";
    private static final String SUCCESS_CODE_STATUS = "status";
    
    @Value("${collect.server.url}")
    private String serverUrl;

    @Scheduled(cron = "0 0/1 * * * ?")
    public void scheduled() {
        List<InformRecord> informRecords =  informRecordRepository.findAllByBizStatus(1);
        informRecords.forEach(i->{
            informServiceImpl.informDingDing(i.getReason()+":"+i.getServerInfoId(),i.getUrl(),serverUrl);
        });
    }
}
