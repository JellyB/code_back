package com.huatu.tiku.teacher.notice.service.fall;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.huatu.tiku.teacher.notice.constant.InnerData;
import com.huatu.tiku.teacher.notice.constant.PushResponse;
import com.huatu.tiku.teacher.notice.service.feign.NoticeFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2018-12-12 下午2:14
 **/
@Slf4j
@Component
public class NoticeFeignFallback implements NoticeFeignClient{

    public static final Cache<Integer,PushResponse> NOTICE_CACHE = CacheBuilder
            .newBuilder()
            .initialCapacity(200)
            .expireAfterWrite(1, TimeUnit.DAYS)
            .maximumSize(1000)
            .build();



    public void put(int userId, PushResponse pushResponse){
        NOTICE_CACHE.put(userId, pushResponse);
    }
    /**
     * 我的notice 列表
     *
     * @param params
     * @return
     */
    @Override
    public PushResponse noticeList(Map<String, Object> params) {
        String userId = String.valueOf(params.get("userId"));
        PushResponse pushResponse = NOTICE_CACHE.getIfPresent(Integer.valueOf(userId));
        if(null == pushResponse){
            InnerData innerData = InnerData.newInstance();
            pushResponse = PushResponse.newInstance(innerData);
        }
        return pushResponse;
    }

    /**
     * 我的未读消息数
     *
     * @param params
     * @return
     */
    @Override
    public PushResponse unReadCount(Map<String, Object> params) {
        return PushResponse.newInstance(0);
    }

    /**
     * 消息已读
     *
     * @param params
     * @return
     */
    @Override
    public PushResponse hasRead(Map<String, Object> params) {
        return PushResponse.newInstance(1);
    }
}
