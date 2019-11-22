package com.huatu.tiku.course.netschool.api.v5;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.util.ResponseUtil;
import com.netflix.hystrix.HystrixCommand;
import feign.hystrix.Fallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 订单相关业务
 * Created by lijun on 2018/6/25
 */
@FeignClient(value = "o-course-service", path = "/lumenapi", fallbackFactory = OrderServiceV5.OrderServiceV5FallbackFactory.class)
public interface OrderServiceV5 {

    /**
     * 获取订单物流信息
     */
    @GetMapping(value = "/v4/common/order/logistics")
    NetSchoolResponse getOrderLogistics(@RequestParam("orderId") int orderId);

    /**
     * 获取最后一条的物流信息
     */
    @GetMapping(value = "/v4/common/order/logistics?type=1")
    NetSchoolResponse getLastOrderLogistics(@RequestParam("orderId") int orderId);

    /**
     * 取消订单
     */
    @PutMapping(value = "/v4/common/order/cacel")
    NetSchoolResponse cancelOrder(@RequestParam Map<String, Object> params);

    /**
     * 删除订单
     */
    @DeleteMapping(value = "/v4/common/order/delete")
    NetSchoolResponse deleteOrder(@RequestParam Map<String, Object> params);

    /**
     * 查询订单详情
     */
    @GetMapping(value = "/v4/common/order/detail")
    NetSchoolResponse detail(@RequestParam Map<String, Object> params);

    /**
     * 查看小程序订单详情
     */
    @GetMapping(value = "/v4/wechat/collage/order_details")
    NetSchoolResponse detailWeChat(@RequestParam Map<String, Object> params);

    /**
     * 查询我的订单列表 - ZTK
     */
    @GetMapping(value = "/v4/common/order/list?isInterview=0")
    NetSchoolResponse userOrderListZTK(@RequestParam HashMap<String, Object> params);

    /**
     * 查询我的订单列表 - IC
     */
    @GetMapping(value = "/v4/common/order/list?isInterview=1")
    NetSchoolResponse userOrderListIC(@RequestParam HashMap<String, Object> params);

    /**
     * 估分送课
     */
    @PostMapping(value = "/v4/common/order/zero_order")
    NetSchoolResponse zeroOrder(@RequestParam Map<String, Object> params);
    
    /**
     * 批量送课
     * @param params
     * @return
     */
    @PostMapping(value = "/v4/common/order/batch_zero_order")
    NetSchoolResponse batchZeroOrder(@RequestParam Map<String, Object> params);

    /**
     * 用户是否已经有估分送课
     */
    @GetMapping(value = "/v4/common/order/has_free")
    NetSchoolResponse hasGetBigGiftOrder(@RequestParam Map<String, Object> params);

    /**
     * 拼团--正在拼团
     * @param params
     * @return
     */
    @GetMapping(value = "/v4/wechat/collage/collage_activity_all")
    NetSchoolResponse activityAll(@RequestParam Map<String, Object> params);

    @Slf4j
    @Component
    class OrderServiceV5FallbackFactory implements Fallback<OrderServiceV5> {
        @Override
        public OrderServiceV5 create(Throwable throwable, HystrixCommand command) {
            return new OrderServiceV5() {
                @Override
                public NetSchoolResponse getOrderLogistics(int orderId) {
                    log.error("order service v5 getOrderLogistics fallback,params: {}, fall back reason: {}",orderId, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                @Override
                public NetSchoolResponse getLastOrderLogistics(int orderId) {
                    log.error("order service v5 getLastOrderLogistics fallback,params: {}, fall back reason: {}",orderId, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                @Override
                public NetSchoolResponse cancelOrder(Map<String, Object> params) {
                    log.error("order service v5 cancelOrder fallback,params: {}, fall back reason: {}",params, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                @Override
                public NetSchoolResponse deleteOrder(Map<String, Object> params) {
                    log.error("order service v5 deleteOrder fallback,params: {}, fall back reason: {}",params, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                @Override
                public NetSchoolResponse detail(Map<String, Object> params) {
                    log.error("order service v5 detail fallback,params: {}, fall back reason: {}",params, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                @Override
                public NetSchoolResponse detailWeChat(Map<String, Object> params) {
                    log.error("order service v5 detailWeChat fallback,params: {}, fall back reason: {}",params, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                @Override
                public NetSchoolResponse userOrderListZTK(HashMap<String, Object> params) {
                    log.error("order service v5 userOrderListZTK fallback,params: {}, fall back reason: {}",params, throwable);
                    return ResponseUtil.DEFAULT_PHP_PAGE_RESPONSE;
                }

                @Override
                public NetSchoolResponse userOrderListIC(HashMap<String, Object> params) {
                    log.error("order service v5 userOrderListIC fallback,params: {}, fall back reason: {}",params, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                @Override
                public NetSchoolResponse zeroOrder(Map<String, Object> params) {
                    log.error("order service v5 zeroOrder fallback,params: {}, fall back reason: {}",params, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                @Override
                public NetSchoolResponse batchZeroOrder(Map<String, Object> params) {
                    log.error("order service v5 batchZeroOrder fallback,params: {}, fall back reason: {}",params, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                @Override
                public NetSchoolResponse hasGetBigGiftOrder(Map<String, Object> params) {
                    log.error("order service v5 hasGetBigGiftOrder fallback,params: {}, fall back reason: {}",params, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                @Override
                public NetSchoolResponse activityAll(Map<String, Object> params) {
                    log.error("order service v5 activityAll fallback,params: {}, fall back reason: {}",params, throwable);
                    return NetSchoolResponse.DEFAULT;
                }
            };
        }
    }
}


