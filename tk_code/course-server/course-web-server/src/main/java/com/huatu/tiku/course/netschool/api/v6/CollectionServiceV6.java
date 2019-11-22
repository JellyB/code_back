package com.huatu.tiku.course.netschool.api.v6;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.util.ResponseUtil;
import com.netflix.hystrix.HystrixCommand;
import feign.hystrix.Fallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 描述：我的收藏接口
 *
 * @author biguodong
 * Create time 2018-11-30 上午10:30
 **/
@FeignClient(value = "o-course-service", path = "/lumenapi", fallbackFactory = CollectionServiceV6.CollectionServiceV6FallbackFactory.class)
public interface CollectionServiceV6 {

    /**
     * 添加我的课程收藏
     * @param params
     * @return
     */
    @PutMapping(value = "/v5/c/class/collection_add")
    NetSchoolResponse add(@RequestParam Map<String, Object> params);

    /**
     * 取消我的课程收藏
     * @param params
     * @return
     */
    @PutMapping(value = "/v5/c/class/collection_cancel")
    NetSchoolResponse cancel(@RequestParam Map<String, Object> params);

    /**
     * 我的收藏课程列表
     * @param params
     * @return
     */
    @GetMapping(value = "/v5/c/class/collection_list")
    NetSchoolResponse list(@RequestParam Map<String, Object> params);



    @Slf4j
    @Component
    class CollectionServiceV6FallbackFactory implements Fallback<CollectionServiceV6>{

        @Override
        public CollectionServiceV6 create(Throwable throwable, HystrixCommand command) {
            return new CollectionServiceV6(){
                /**
                 * 添加我的课程收藏
                 *
                 * @param params
                 * @return
                 */
                @Override
                public NetSchoolResponse add(Map<String, Object> params) {
                    log.error("collection service v6 add fallback,params: {}, fall back reason: {}",params, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                /**
                 * 取消我的课程收藏
                 *
                 * @param params
                 * @return
                 */
                @Override
                public NetSchoolResponse cancel(Map<String, Object> params) {
                    log.error("collection service v6 cancel fallback,params: {}, fall back reason: {}",params, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                /**
                 * 我的收藏课程列表
                 *
                 * @param params
                 * @return
                 */
                @Override
                public NetSchoolResponse list(Map<String, Object> params) {
                    log.error("collection service v6 list fallback,params: {}, fall back reason: {}",params, throwable);
                    return ResponseUtil.DEFAULT_PHP_PAGE_RESPONSE;
                }
            };
        }
    }
}
