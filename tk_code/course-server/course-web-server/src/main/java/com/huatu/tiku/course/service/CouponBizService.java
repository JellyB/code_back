package com.huatu.tiku.course.service;

import com.alibaba.fastjson.TypeReference;
import com.google.common.primitives.Ints;
import com.huatu.common.utils.collection.HashMapBuilder;
import com.huatu.tiku.course.bean.CouponV3DTO;
import com.huatu.tiku.course.netschool.api.v3.CouponServiceV3;
import com.huatu.tiku.course.util.RequestUtil;
import com.huatu.tiku.course.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.Map;

/**
 * @author hanchao
 * @date 2017/9/13 17:38
 */
@Service
@Slf4j
public class CouponBizService {
    @Autowired
    private CouponServiceV3 couponServiceV3;

    /**
     * 获取所有的兑换券列表
     * @param div
     * @return
     */
    @Async
    public ListenableFuture<Map<String,List<CouponV3DTO>>> findCouponList(int div){
        Map param = HashMapBuilder.newBuilder().put("div",div).build();
        Map<String,List<CouponV3DTO>> result = ResponseUtil.build(couponServiceV3.findCouponList(RequestUtil.encrypt(param)),new TypeReference<Map<String,List<CouponV3DTO>>>(){},true);
        return new AsyncResult<>(result);
    }

    /**
     * 获取所有兑换券的已兑换数量
     * @return
     */
    @Async
    public ListenableFuture<Map<String,Integer>> findCouponSaleNums(){
        Map<String,Map<String,Object>> result = ResponseUtil.build(couponServiceV3.findCouponSaleNums(),new TypeReference<Map<String,Map<String,Object>>>(){},true);
        result.values().forEach(x -> {
            if(x != null){
                x.forEach( (k,v) -> {
                    x.put(k, Ints.tryParse(String.valueOf(v)));
                });
            }
        });
        return new AsyncResult(result.get("result"));
    }
}
