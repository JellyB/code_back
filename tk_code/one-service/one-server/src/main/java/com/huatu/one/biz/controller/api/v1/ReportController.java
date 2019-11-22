package com.huatu.one.biz.controller.api.v1;

import com.google.common.collect.Lists;
import com.huatu.one.biz.service.ReportService;
import com.huatu.one.biz.service.UsageRecordService;
import com.huatu.one.biz.vo.DataAchievementResponse;
import com.huatu.one.biz.vo.DataAchievementV1Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 报表
 *
 * @author geek-s
 * @date 2019-08-29
 */
@RestController
@RequestMapping("/v1/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private UsageRecordService usageRecordService;

    /**
     * 获取数据报表
     *
     * @param openid 微信ID
     * @return 报表
     */
    @GetMapping
    public Object list(@RequestHeader String openid) {
        usageRecordService.saveRecord(openid, 1);

        List<DataAchievementResponse> achievementResponses = reportService.list(openid);

        achievementResponses.forEach(achievementResponse -> {
            DataAchievementResponse.ReportData reportData = achievementResponse.getRows().get(0);
            reportData.setToday(new BigDecimal(reportData.getToday()).divide(new BigDecimal(10000), 2, BigDecimal.ROUND_HALF_UP).toString());
            reportData.setWeek(new BigDecimal(reportData.getWeek()).divide(new BigDecimal(10000), 2, BigDecimal.ROUND_HALF_UP).toString());
            reportData.setMonth(new BigDecimal(reportData.getMonth()).divide(new BigDecimal(10000), 2, BigDecimal.ROUND_HALF_UP).toString());
            reportData.setYesterday(new BigDecimal(reportData.getYesterday()).divide(new BigDecimal(10000), 2, BigDecimal.ROUND_HALF_UP).toString());
        });

        return achievementResponses;
    }

    /**
     * 获取数据报表
     *
     * @param category 数据分类
     * @return 报表
     */
    @GetMapping("detailV1")
    public Object detailV1(@RequestParam Long category, @RequestHeader String openid) {
        List<DataAchievementV1Response> achievementV1Responses = reportService.detailV1(category, openid);

        DataAchievementV1Response.ReportData reportData = achievementV1Responses.get(0).getRows().get(0);

        reportData.setWeek(new BigDecimal(reportData.getWeek()).divide(new BigDecimal(10000), 2, BigDecimal.ROUND_HALF_UP).toString());
        reportData.setMonth(new BigDecimal(reportData.getMonth()).divide(new BigDecimal(10000), 2, BigDecimal.ROUND_HALF_UP).toString());

        return achievementV1Responses.get(0).getRows();
    }

    /**
     * 获取数据报表
     *
     * @param category 数据分类
     * @return 报表
     */
    @GetMapping("detailV2")
    public Object detailV2(@RequestParam Long category, @RequestHeader String openid) {
        return Lists.newArrayList(reportService.detailV2(category, 7, openid), reportService.detailV2(category, 30, openid));
    }
}
