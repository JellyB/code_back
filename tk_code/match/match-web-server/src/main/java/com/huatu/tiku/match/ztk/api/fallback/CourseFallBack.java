package com.huatu.tiku.match.ztk.api.fallback;

import com.google.common.collect.Maps;
import com.huatu.tiku.match.common.FeignResponse;
import com.huatu.tiku.match.ztk.api.CourseFeignClient;
import com.huatu.ztk.paper.common.ResponseMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-01-09 下午6:41
 **/
@Component(value = "courseFallBack")
@Slf4j
public class CourseFallBack implements CourseFeignClient{

    /**
     * 获取解析课课程信息
     *
     * @param params
     * @return
     */
    @Override
    public FeignResponse analysis(Map<String, Object> params) {
        int classId = Integer.valueOf(String.valueOf(params.get(CLASS_ID)));
        LinkedHashMap<String,Object> result = Maps.newLinkedHashMap();
        result.put(CLASS_ID, classId);
        result.put(COURSE_TITLE, "");
        result.put(LIVE_DATE, System.currentTimeMillis());
        result.put(PRICE, 0);
        return FeignResponse.newInstance(result);

    }

    /**
     * 是否领取课程
     *
     * @param params userName、classId
     * @return
     */
    @Override
    public ResponseMsg<Object> isHasGet(Map<String, Object> params) {
        return ResponseMsg.builder().build();
    }
}
