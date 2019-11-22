package com.huatu.tiku.course.netschool.api.v3;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.fall.PromoteCoreServiceV3FallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 促销时候的核心线程池，独立出来方便隔离
 * @author hanchao
 * @date 2017/10/18 10:19
 */
@FeignClient(value = "course-core-service",fallbackFactory = PromoteCoreServiceV3FallbackFactory.class)
public interface PromoteCoreServiceV3 {
    /**
     * 下单页面获取信息（action固定传placeOrder）
     * @param p (action  username rid )
     * @return
     */
    @PostMapping("/v3/order/order.php")
    NetSchoolResponse getPrevInfo(@RequestParam("p") String p);

    /**
     * 订单详情
     * @param p
     * @return
     */
    @PostMapping("/v3/order/order.php")
    NetSchoolResponse getOrderDetail(@RequestParam("p") String p);

    /**
     * 下单接口
     * @param p
     * @return
     */
    @PostMapping("/v3/order/order.php")
    NetSchoolResponse createOrder(@RequestParam("p") String p);

    /**
     * 支付接口
     * @param p
     * @return
     */
    @PostMapping("/v3/order/pay.php")
    NetSchoolResponse payOrder(@RequestParam("p") String p);
}
