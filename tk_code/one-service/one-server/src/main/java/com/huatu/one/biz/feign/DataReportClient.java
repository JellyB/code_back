package com.huatu.one.biz.feign;

import com.huatu.one.biz.vo.DataAchievementResponse;
import com.huatu.one.biz.vo.DataAchievementV1Response;
import com.huatu.one.biz.vo.DataAchievementV2Response;
import com.huatu.one.biz.vo.DataResponseWrapper;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 大数据
 *
 * @author geek-s
 * @date 2019-08-29
 */
@FeignClient(name = "data-report", url = "${data-report.ribbon.listOfServers}")
public interface DataReportClient {

    /**
     * 获取数据报表
     *
     * @param category 数据类型
     * @param token    令牌
     * @return 数据报表
     */
    @GetMapping("/achievement")
    DataResponseWrapper<DataAchievementResponse> achievement(@RequestParam("category") Long[] category, @RequestHeader("token") String token);

    /**
     * 获取数据报表
     *
     * @param category 数据类型
     * @param token    令牌
     * @return 数据报表
     */
    @GetMapping("/achievement/v1")
    DataResponseWrapper<DataAchievementV1Response> achievementV1(@RequestParam("category") Long category, @RequestHeader("token") String token);

    /**
     * 获取数据报表
     *
     * @param category 数据类型
     * @param token    令牌
     * @param status   7/30天
     * @return 数据报表
     */
    @GetMapping("/achievement/v2")
    DataResponseWrapper<DataAchievementV2Response> achievementV2(@RequestParam("category") Long category, @RequestParam("status") Integer status, @RequestHeader("token") String token);
}
