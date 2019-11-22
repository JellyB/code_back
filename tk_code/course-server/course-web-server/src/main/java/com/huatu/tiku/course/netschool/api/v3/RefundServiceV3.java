package com.huatu.tiku.course.netschool.api.v3;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @author hanchao
 * @date 2017/9/28 13:18
 */
@FeignClient(value = "course-service")
public interface RefundServiceV3 {
    /**
     * 提交退款申请
     * @param p
     * @return
     */
    @PostMapping("/v3/order/refund.php")
    NetSchoolResponse submitRefund(@RequestParam("p") String p);

    /**
     * 查看退款详情
     * @return
     */
    @GetMapping("/v3/order/refundDetail.php")
    NetSchoolResponse getRefundDetail(@RequestParam Map<String,Object> params);
}
