package com.huatu.tiku.course.netschool.api.v6;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.netflix.hystrix.HystrixCommand;
import feign.hystrix.Fallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-10-28 9:52 AM
 **/

@FeignClient(value = "o-course-service", path = "/lumenapi", fallbackFactory = OrderServiceV6.OrderServiceV6FallBackFactory.class)
public interface OrderServiceV6 {


    /**
     * 0 元下单
     */
    @PostMapping(value = "/v5/c/order/zero_order")
    NetSchoolResponse zeroOrder(@RequestParam Map<String, Object> params);

    /**
     * 下单支付
     */
    @PostMapping(value = "/v5/c/order/create_order")
    NetSchoolResponse createOrder(@RequestParam Map<String, Object> params);

    /**
     * 订单--是否支付成功
     * @param params
     * @return
     */
    @GetMapping(value = "/v5/c/order/order_status")
    NetSchoolResponse orderStatus(@RequestParam Map<String, Object> params);


    /**
     * 订单--继续支付
     * @param params
     * @return
     */
    @PostMapping(value = "/v5/c/order/order_continue_pay")
    NetSchoolResponse continuePay(@RequestParam Map<String, Object> params);



    @GetMapping(value = "/v5/c/order/place_order")
    NetSchoolResponse placeOrder(@RequestParam Map<String, Object> params);

    @PostMapping(value = "/v5/c/order/recharge_gold")
    NetSchoolResponse reCharge(@RequestParam Map<String, Object> params);

    @Component
    @Slf4j
    class OrderServiceV6FallBackFactory implements Fallback<OrderServiceV6>{

        @Override
        public OrderServiceV6 create(Throwable throwable, HystrixCommand command) {
            return new OrderServiceV6() {
                @Override
                public NetSchoolResponse zeroOrder(Map<String, Object> params) {
                    log.error("order service v6. zero order failed, params:{}, reason:{}", params, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                @Override
                public NetSchoolResponse createOrder(Map<String, Object> params) {
                    log.error("order service v6. create order failed, params:{}, reason:{}", params, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                @Override
                public NetSchoolResponse orderStatus(Map<String, Object> params) {
                    log.error("order service v6. order status failed, params:{}, reason:{}", params, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                @Override
                public NetSchoolResponse continuePay(Map<String, Object> params) {
                    log.error("order service v6. continue order failed, params:{}, reason:{}", params, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                @Override
                public NetSchoolResponse placeOrder(Map<String, Object> params) {
                    log.error("order service v6. place order failed, params:{}, reason:{}", params, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                @Override
                public NetSchoolResponse reCharge(Map<String, Object> params) {
                    log.error("order service v6. reCharge order failed, params:{}, reason:{}", params, throwable);
                    return NetSchoolResponse.DEFAULT;
                }
            };
        }
    }
}
