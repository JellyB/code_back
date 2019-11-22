package com.huatu.tiku.course.netschool.api.v3;

/**
 * @author hanchao
 * @date 2017/9/13 16:59
 */

import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.netflix.hystrix.HystrixCommand;
import feign.hystrix.Fallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(value = "course-service", fallbackFactory = UserAccountServiceV3.UserAccountServiceV3FallBack.class)
public interface UserAccountServiceV3 {
    /**
     * 我的兑换券列表
     * @param p (action 1，正常 2，过期  username)
     * @return
     */
    @PostMapping("/v3/mycount/myVoucher.php")
    NetSchoolResponse findUserCouponList(@RequestParam("p") String p);


    /**
     * 获取用户积分余额（action固定传1）
     * @param p (action 1，余额 2，列表  username)
     * @return
     */
    @PostMapping("/v3/mycount/myPoint.php")
    NetSchoolResponse getUserPointLeft(@RequestParam("p") String p);

    /**
     * 获取用户积分记录（action固定传2）
     * @param p (action 1，余额 2，列表  username  page)
     * @return
     */
    @PostMapping("/v3/mycount/myPoint.php")
    NetSchoolResponse findUserPointRecords(@RequestParam("p") String p);

    /**
     * 积分兑换代金券
     * @param p
     * @return
     */
    @PostMapping("/v3/mycount/VocherByIntegrate.php")
    NetSchoolResponse exchangeCoupon(@RequestParam("p") String p);

    /**
     * 账户 消费/充值 记录
     * @param p (action 2，充值 3，消费 username page)
     * @return
     */
    @PostMapping("/v3/mycount/myCount.php")
    NetSchoolResponse findAccountRecortds(@RequestParam("p") String p);

    /**
     * 账户 消费/充值 记录
     * @param p (action 1，余额 username page)
     * @return
     */
    @PostMapping("/v3/mycount/myCount.php")
    NetSchoolResponse getAccountBalance(@RequestParam("p") String p);

    /**
     * 充值
     * @return
     */
    @PostMapping("/v3/wxPay/pay/js_api_call_app.php")
    NetSchoolResponse chargeAccount(@RequestParam Map<String,Object> params);

    @Component
    @Slf4j
    class UserAccountServiceV3FallBack implements Fallback<UserAccountServiceV3>{

        @Override
        public UserAccountServiceV3 create(Throwable throwable, HystrixCommand command) {
            return new UserAccountServiceV3(){
                /**
                 * 我的兑换券列表
                 *
                 * @param p (action 1，正常 2，过期  username)
                 * @return
                 */
                @Override
                public NetSchoolResponse findUserCouponList(String p) {
                    log.error("UserAccountServiceV3 findUserCouponList fall back, params:{}, reason:{}", p, throwable);
                    return NetSchoolResponse.DEFAULT_ERROR;
                }

                /**
                 * 获取用户积分余额（action固定传1）
                 *
                 * @param p (action 1，余额 2，列表  username)
                 * @return
                 */
                @Override
                public NetSchoolResponse getUserPointLeft(String p) {
                    log.error("UserAccountServiceV3 getUserPointLeft fall back, params:{}, reason:{}", p, throwable);
                    return NetSchoolResponse.DEFAULT_ERROR;
                }

                /**
                 * 获取用户积分记录（action固定传2）
                 *
                 * @param p (action 1，余额 2，列表  username  page)
                 * @return
                 */
                @Override
                public NetSchoolResponse findUserPointRecords(String p) {
                    log.error("UserAccountServiceV3 findUserPointRecords fall back, params:{}, reason:{}", p, throwable);
                    return NetSchoolResponse.DEFAULT_ERROR;
                }

                /**
                 * 积分兑换代金券
                 *
                 * @param p
                 * @return
                 */
                @Override
                public NetSchoolResponse exchangeCoupon(String p) {
                    log.error("UserAccountServiceV3 exchangeCoupon fall back, params:{}, reason:{}", p, throwable);
                    return NetSchoolResponse.DEFAULT_ERROR;
                }

                /**
                 * 账户 消费/充值 记录
                 *
                 * @param p (action 2，充值 3，消费 username page)
                 * @return
                 */
                @Override
                public NetSchoolResponse findAccountRecortds(String p) {
                    log.error("UserAccountServiceV3 findAccountRecortds fall back, params:{}, reason:{}", p, throwable);
                    return NetSchoolResponse.DEFAULT_ERROR;
                }

                /**
                 * 账户 消费/充值 记录
                 *
                 * @param p (action 1，余额 username page)
                 * @return
                 */
                @Override
                public NetSchoolResponse getAccountBalance(String p) {
                    log.error("UserAccountServiceV3 getAccountBalance fall back, params:{}, reason:{}", p, throwable);
                    return NetSchoolResponse.DEFAULT_ERROR;
                }

                /**
                 * 充值
                 *
                 * @param params
                 * @return
                 */
                @Override
                public NetSchoolResponse chargeAccount(Map<String, Object> params) {
                    log.error("UserAccountServiceV3 chargeAccount fall back, params:{}, reason:{}", params, throwable);
                    return NetSchoolResponse.DEFAULT_ERROR;
                }
            };
        }
    }
}
