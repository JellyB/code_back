package com.huatu.tiku.course.service.v1;

import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import java.util.concurrent.TimeUnit;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-03-15 5:43 PM
 * Create time 2019-03-17 11:43 AM
 **/

@Service
@Slf4j
public class AccessLimitService {

    @Value("${my.course.rate.limit.v6}")
    private int rateLimit;

    @Value("${my.course.rate.limit.timeOut.v6}")
    private int timeOut;

    private RateLimiter rateLimiter;


    public boolean tryAccess(){
        return rateLimiter.tryAcquire(timeOut, TimeUnit.MILLISECONDS);
    }


    @PostConstruct
    public void init(){
        rateLimiter = RateLimiter.create(rateLimit);
    }
}
