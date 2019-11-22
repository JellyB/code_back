package com.huatu.tiku.course.netschool.api.v3;

import com.huatu.common.spring.web.MediaType;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @author hanchao
 * @date 2017/9/19 14:00
 */
@FeignClient(value = "course-service")
public interface AddressServiceV3 {
    /**
     * 获取用户地址列表  action->getAddress
     * @param p
     * @return
     */
    @GetMapping("/v3/order/order.php")
    NetSchoolResponse findAddressList(@RequestParam("p")String p);


    /**
     * 创建地址 action->addAddress
     * @param params
     * @return
     */
    @PostMapping(value = "/v3/order/order.php",consumes = MediaType.APPLICATION_FORM_URLENCODED_UTF8_VALUE)
    NetSchoolResponse createAddress(Map<String,Object> params);

    /**
     * 修改地址 action->modifyAddress
     * @param p
     * @return
     */
    @PostMapping(value = "/v3/order/order.php",consumes = MediaType.APPLICATION_FORM_URLENCODED_UTF8_VALUE)
    NetSchoolResponse updateAddress(Map<String,Object> p);

    /**
     * 删除地址 action->delAddress
     * @param p
     * @return
     */
    @PostMapping("/v3/order/order.php")
    NetSchoolResponse deleteAddress(@RequestParam("p")String p);
}
