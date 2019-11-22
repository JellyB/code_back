package com.huatu.tiku.course.netschool.api.v3;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @author hanchao
 * @date 2017/9/20 11:27
 */
@FeignClient(value = "course-service")
public interface LogisticsServiceV3 {
    /**
     * 物流详情 action->getLogistics
     * @param p
     * @return
     */
    @PostMapping("/v3/order/order.php")
    NetSchoolResponse getLogisticsDetail(@RequestParam("p") String p);


    /**
     * 物流列表接口
     * @param params
     * @return
     */
    @RequestMapping(value = "/v3/logisticsInfo.php",method = RequestMethod.GET)
    NetSchoolResponse queryList(@RequestParam Map<String,Object> params);
}
