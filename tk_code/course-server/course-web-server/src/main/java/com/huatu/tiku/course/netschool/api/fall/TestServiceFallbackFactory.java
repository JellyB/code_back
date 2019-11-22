package com.huatu.tiku.course.netschool.api.fall;

import org.springframework.stereotype.Component;

import com.huatu.tiku.course.netschool.api.TestService;
import com.netflix.hystrix.HystrixCommand;

import feign.hystrix.Fallback;
import lombok.extern.slf4j.Slf4j;

/**
 * @author hanchao
 * @date 2017/9/5 18:04
 */
@Component
@Slf4j
public class TestServiceFallbackFactory implements Fallback<TestService> {
    @Override
    public TestService create(Throwable cause, HystrixCommand command) {
        return new TestService() {
            @Override
            public String courseDetail(int rid) {
                log.error("TestService  v1 fallback,params: {}, fall back reason: {}",rid,cause);
                return "服务器人太多了....";
            }
        };
    }
}
