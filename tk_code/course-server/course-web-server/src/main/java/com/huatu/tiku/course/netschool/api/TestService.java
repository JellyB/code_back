package com.huatu.tiku.course.netschool.api;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.huatu.tiku.course.netschool.api.fall.TestServiceFallbackFactory;

/**
 * 
 * @author zhangchong
 *
 */
@FeignClient(url="http://127.0.0.1:10917",value="test",path="/c",fallbackFactory = TestServiceFallbackFactory.class)
public interface TestService {
    @RequestMapping(value = "/test/hystrix",method = RequestMethod.GET )
    Object courseDetail(@RequestParam("rid")int rid);
}
