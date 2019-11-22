package com.huatu.tiku.course.netschool.api;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.netflix.hystrix.HystrixCommand;
import feign.hystrix.Fallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * 描述：es 搜索接口
 *
 * @author biguodong
 * Create time 2019-09-10 上午10:30
 **/
@FeignClient(value = "ztk-search-service", path = "/s", fallbackFactory = SearchServiceV1.SearchServiceV1FallbackFactory.class)
public interface SearchServiceV1 {

    /**
     * 我的收藏课程列表
     * @param token
     * @param keyWord
     * @return
     */
    @GetMapping(value = "/v1/search/course/keywords/up")
    NetSchoolResponse upSetKeyWord(@RequestHeader(value = "token") String token,
                                   @RequestParam(value = "keyWord")String keyWord);



    @Slf4j
    @Component
    class SearchServiceV1FallbackFactory implements Fallback<SearchServiceV1>{

        @Override
        public SearchServiceV1 create(Throwable throwable, HystrixCommand command) {
            return new SearchServiceV1(){
                /**
                 * 我的收藏课程列表
                 *
                 * @param token
                 * @param keyWord
                 * @return
                 */
                @Override
                public NetSchoolResponse upSetKeyWord(String token, String keyWord) {
                    log.error("search service v1 upSetKeyWord fallback,params: token:{}, keyWord:{}, fall back reason: {}", token, keyWord, throwable);
                    return NetSchoolResponse.DEFAULT;
                }
            };
        }
    }
}
