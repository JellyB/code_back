package com.huatu.tiku.course.netschool.api.fall;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.v3.OrderServiceV3;
import com.netflix.hystrix.HystrixCommand;
import feign.hystrix.Fallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author hanchao
 * @date 2017/10/4 12:53
 */
@Component
@Slf4j
public class OrderServiceV3FallbackFactory  implements Fallback<OrderServiceV3> {
    @Override
    public OrderServiceV3 create(Throwable cause,HystrixCommand command) {
        return new OrderServiceV3() {
            @Override
            public NetSchoolResponse getPrevInfo(String p) {
                return NetSchoolResponse.DEFAULT_ERROR;
            }

            @Override
            public NetSchoolResponse getOrderDetail(String p) {
                return NetSchoolResponse.DEFAULT_ERROR;
            }

            @Override
            public NetSchoolResponse createOrder(String p) {
                return NetSchoolResponse.DEFAULT_ERROR;
            }

            @Override
            public NetSchoolResponse cancelOrder(String p) {
                return NetSchoolResponse.DEFAULT_ERROR;
            }

            @Override
            public NetSchoolResponse myOrder(String p) {
                return NetSchoolResponse.DEFAULT_ERROR;
            }

            @Override
            public NetSchoolResponse payOrder(String p) {
                return NetSchoolResponse.DEFAULT_ERROR;
            }

            @Override
            public NetSchoolResponse getFree(Map<String, Object> params) {
                return NetSchoolResponse.DEFAULT_ERROR;
            }
        };
    }
}
