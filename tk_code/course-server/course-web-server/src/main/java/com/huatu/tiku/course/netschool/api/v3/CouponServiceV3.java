package com.huatu.tiku.course.netschool.api.v3;

/**
 * @author hanchao
 * @date 2017/9/13 16:29
 */

import com.huatu.tiku.course.bean.NetSchoolResponse;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "course-service")
public interface CouponServiceV3 {
    /**
     *
     * @param p div 设备类型 0，安卓  1，IOS
     * @return
     */
    @PostMapping("/v3/mycount/GetVoucher.php")
    NetSchoolResponse findCouponList(@RequestParam("p")String p);

    /**
     * 返回代金券兑换数量
     * @return
     */
    @PostMapping("/v3/mycount/VouchersExchangeNum.php")
    NetSchoolResponse findCouponSaleNums();
}
