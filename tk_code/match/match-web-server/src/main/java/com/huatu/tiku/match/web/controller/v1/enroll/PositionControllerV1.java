package com.huatu.tiku.match.web.controller.v1.enroll;

import com.alibaba.fastjson.JSONObject;
import com.huatu.springboot.web.version.mapping.annotation.ApiVersion;
import com.huatu.tiku.match.common.FeignResponse;
import com.huatu.tiku.match.ztk.api.PositionFeignClient;
import com.huatu.tiku.match.ztk.api.fallback.PositionFallBack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-01-03 下午12:06
 **/
@RestController
@RequestMapping(value = "positions")
@ApiVersion(value = "v1")
@Slf4j
public class PositionControllerV1 {

    private static final String PARENT = "parent";
    private static final String CHILDRENS = "childrens";

    @Autowired
    private PositionFeignClient positionFeignClient;

    @Autowired
    private PositionFallBack positionFallBack;

    @GetMapping
    public Object getPositions(){
        FeignResponse feignResponse;
        if(null != PositionFallBack.cacheValue()){
            feignResponse = PositionFallBack.cacheValue();
            return feignResponse.getData();
        }else{
            feignResponse =  positionFeignClient.getPositions();
            List<LinkedHashMap> data = (List<LinkedHashMap>) feignResponse.getData();
            if(data.get(0).containsKey(PARENT)){
                for(LinkedHashMap linkedHashMap : data){
                    linkedHashMap.remove(PARENT);
                    linkedHashMap.remove(CHILDRENS);
                }
                feignResponse.setData(data);
            }
            positionFallBack.cache(feignResponse);
            log.info(JSONObject.toJSONString(feignResponse.getData()));
            return feignResponse.getData();
        }
    }
}
