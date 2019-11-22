package com.huatu.one.biz.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 大数据报表
 *
 * @author geek-s
 * @date 2019-09-12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataAchievementV2Response {

    /**
     * 数据
     */
    private List<OrderData> mars;

    /**
     * 数据
     */
    private List<ReportData> rows;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderData {

        private BigDecimal money;

        private String price;

        private String rate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReportData {

        private String price;

        private String rate;

        private String some;
    }
}
