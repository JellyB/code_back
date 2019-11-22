package com.huatu.tiku.teacher.notice.service.feign;

import com.huatu.tiku.teacher.notice.constant.PushResponse;
import com.huatu.tiku.teacher.notice.service.fall.NoticeFeignFallback;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2018-12-06 下午2:23
 **/
@FeignClient(value = "t-push-server", path = "/push", fallback = NoticeFeignFallback.class)
public interface NoticeFeignClient {

    /**
     * 我的notice 列表
     * @param params
     * @return
     */
    @GetMapping("/v1/feign/noticeList")
    PushResponse noticeList(@RequestParam Map<String,Object> params);

    /**
     * 我的未读消息数
     * @param params
     * @return
     */
    @GetMapping(value = "/v1/feign/unReadCount")
    PushResponse unReadCount(@RequestParam Map<String,Object> params);

    /**
     * 消息已读
     * @param params
     * @return
     */
    @PutMapping(value = "/v1/feign/hasRead")
    PushResponse hasRead(@RequestParam Map<String,Object> params);
}
