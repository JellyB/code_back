package com.huatu.tiku.course.web.controller.v3;

import com.huatu.common.exception.BizException;
import com.huatu.common.utils.date.TimeMark;
import com.huatu.tiku.course.bean.CouponV3DTO;
import com.huatu.tiku.course.consts.NetschoolTerminalType;
import com.huatu.tiku.course.service.CouponBizService;
import com.huatu.tiku.course.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * @author hanchao
 * @date 2017/9/13 16:38
 */
@RestController
@RequestMapping("/v3/coupon")
@Slf4j
public class CouponControllerV3 {

    @Autowired
    private CouponBizService couponBizService;

    /**
     * 兑换券列表
     * @param terminal
     * @return
     */
    @GetMapping("/list")
    public Object findCouponList(@RequestHeader int terminal) throws BizException, ExecutionException, InterruptedException {
        TimeMark timeMark = TimeMark.newInstance();
        int div = NetschoolTerminalType.transform(terminal);

        ListenableFuture<Map<String, List<CouponV3DTO>>> couponListFuture = couponBizService.findCouponList(div);
        ListenableFuture<Map<String, Integer>> couponSaleNumsFuture = couponBizService.findCouponSaleNums();

        Map<String, List<CouponV3DTO>> couponList = couponListFuture.get();
        Map<String, Integer> couponSaleNums = couponSaleNumsFuture.get();

        log.info(">>>>>>>>> couponlist: concurent request complete,used {} mills,total cost {} mills...",timeMark.mills(),timeMark.totalMills());

        if(couponList == null || !couponList.containsKey("voucher")){
            throw new BizException(ResponseUtil.ERROR_NULL_RESPONSE);
        }
        if(couponSaleNums != null){
            for (CouponV3DTO coupon : couponList.get("voucher")) {
                coupon.setSales(Optional.ofNullable(couponSaleNums.get(coupon.getVoucherid())).orElse(0));
            }
        }
        return couponList;
    }
}
