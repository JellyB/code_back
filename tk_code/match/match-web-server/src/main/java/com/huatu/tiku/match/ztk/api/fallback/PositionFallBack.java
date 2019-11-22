package com.huatu.tiku.match.ztk.api.fallback;

import com.alibaba.fastjson.JSONArray;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.huatu.tiku.match.common.FeignResponse;
import com.huatu.tiku.match.ztk.api.PositionFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-01-03 下午12:09
 **/

@Component(value = "positionFallBack")
@Slf4j
public class PositionFallBack implements PositionFeignClient{

    private static final String DATA = "[{\"id\":1,\"name\":\"北京\"},{\"id\":21,\"name\":\"天津\"},{\"id\":41,\"name\":\"河北\"},{\"id\":225,\"name\":\"山西\"},{\"id\":356,\"name\":\"内蒙古\"},{\"id\":471,\"name\":\"辽宁\"},{\"id\":586,\"name\":\"吉林\"},{\"id\":656,\"name\":\"黑龙江\"},{\"id\":802,\"name\":\"上海\"},{\"id\":823,\"name\":\"江苏\"},{\"id\":943,\"name\":\"浙江\"},{\"id\":1045,\"name\":\"安徽\"},{\"id\":1168,\"name\":\"福建\"},{\"id\":1263,\"name\":\"江西\"},{\"id\":1374,\"name\":\"山东\"},{\"id\":1532,\"name\":\"河南\"},{\"id\":1709,\"name\":\"湖北\"},{\"id\":1826,\"name\":\"湖南\"},{\"id\":1963,\"name\":\"广东\"},{\"id\":2106,\"name\":\"广西\"},{\"id\":2230,\"name\":\"海南\"},{\"id\":2257,\"name\":\"重庆\"},{\"id\":2299,\"name\":\"四川\"},{\"id\":2502,\"name\":\"贵州\"},{\"id\":2600,\"name\":\"云南\"},{\"id\":2746,\"name\":\"西藏\"},{\"id\":2827,\"name\":\"陕西\"},{\"id\":2945,\"name\":\"甘肃\"},{\"id\":3046,\"name\":\"青海\"},{\"id\":3098,\"name\":\"宁夏\"},{\"id\":3125,\"name\":\"新疆\"}]";


    public static final Cache<Integer, FeignResponse> POSITION_CACHE = CacheBuilder
            .newBuilder()
            .initialCapacity(2)
            .expireAfterWrite(7, TimeUnit.DAYS)
            .build();

    /**
     * 代理paper服务地区接口
     *
     * @return
     */
    @Override
    public FeignResponse getPositions() {

        if(null != POSITION_CACHE.getIfPresent(0)){
            log.error("> obtain position info from fallback cache");
            return POSITION_CACHE.getIfPresent(0);
        }else{
            log.error("> obtain position info from fallback static data");
            List<LinkedHashMap> list = JSONArray.parseArray(DATA, LinkedHashMap.class);
            return FeignResponse.newInstance(list);
        }
    }

    public static FeignResponse cacheValue(){
        if(null != POSITION_CACHE.getIfPresent(0)){
            return POSITION_CACHE.getIfPresent(0);
        }else{
            return null;
        }
    }


    public void cache(FeignResponse feignResponse){
        POSITION_CACHE.put(0, feignResponse);
    }
}
