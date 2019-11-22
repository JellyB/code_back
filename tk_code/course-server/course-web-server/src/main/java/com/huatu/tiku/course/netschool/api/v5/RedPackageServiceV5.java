package com.huatu.tiku.course.netschool.api.v5;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Created by lijun on 2018/9/28
 */
@FeignClient(value = "o-course-service", path = "/lumenapi/v4/common/red/")
public interface RedPackageServiceV5 {

    /**
     * 支付成功插入待领取红包接口及返回红包信息（已废弃）
     * 失败 PHP直接出异常
     */
    @PostMapping(value = "add")
    NetSchoolResponse add(@RequestParam Map<String, Object> params);

    /**
     * 红包个人中心
     */
    @GetMapping(value = "redEnvelope_core")
    NetSchoolResponse redEnvelopeCore(@RequestParam Map<String, Object> params);

    /**
     * 红包领取详情
     */
    @GetMapping(value = "receive_details")
    NetSchoolResponse receiveDetails(@RequestParam Map<String, Object> params);

    /**
     * 获取可发红包详情
     */
    @GetMapping(value = "detail")
    NetSchoolResponse detail(@RequestParam Map<String, Object> params);

    /**
     * 判断用户是否有发起红包或领取红包
     */
    @GetMapping(value = "check_redEnv")
    NetSchoolResponse checkRedEnv(@RequestParam Map<String, Object> params);

    /**
     * 微信提现
     */
    @PostMapping(value = "withdraw_wechat")
    NetSchoolResponse withdrawWechat(@RequestParam Map<String, Object> params);

    /**
     * 支付宝提现
     */
    @PostMapping(value = "withdraw_ali")
    NetSchoolResponse withdrawAli(@RequestParam Map<String, Object> params);

    /**
     * 判断红包是否显示
     */
    @GetMapping(value = "show_redEnv")
    NetSchoolResponse showRedEvn();
}
