package com.huatu.tiku.course.test;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.huatu.common.exception.BizException;
import com.huatu.common.test.BaseWebTest;
import com.huatu.common.utils.date.TimeMark;
import com.huatu.tiku.course.bean.CouponV3DTO;
import com.huatu.tiku.course.service.CouponBizService;
import com.huatu.tiku.course.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * @author hanchao
 * @date 2017/9/23 10:38
 */
@Slf4j
public class CouponTest extends BaseWebTest {
    @Autowired
    private CouponBizService couponBizService;
    @Test
    public void testCouponList() throws InterruptedException, ExecutionException, BizException {
        TimeMark timeMark = TimeMark.newInstance();
        ListenableFuture<Map<String, List<CouponV3DTO>>> couponListFuture = couponBizService.findCouponList(1);
        ListenableFuture<Map<String, Integer>> couponSaleNumsFuture = couponBizService.findCouponSaleNums();

        Map<String, List<CouponV3DTO>> couponList = couponListFuture.get();
        Map<String, Integer> couponSaleNums = couponSaleNumsFuture.get();

        log.info(">>>>>>>>> couponlist: concurent request complete,used {} mills,total cost {} mills...",timeMark.mills(),timeMark.totalMills());

        if(couponList == null || !couponList.containsKey("voucher")){
            throw new BizException(ResponseUtil.ERROR_NULL_RESPONSE);
        }
        for (CouponV3DTO coupon : couponList.get("voucher")) {
            coupon.setSales(Optional.ofNullable(couponSaleNums.get(coupon.getVoucherid())).orElse(0));
        }
        log.info(JSON.toJSONString(couponList, SerializerFeature.PrettyFormat));
    }
}
