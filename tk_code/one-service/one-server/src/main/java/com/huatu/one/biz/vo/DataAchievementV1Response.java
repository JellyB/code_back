package com.huatu.one.biz.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class DataAchievementV1Response {

    /**
     * 业绩类型
     */
    private String name;

    /**
     * 数据
     */
    private List<ReportData> rows;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReportData {

        /**
         * 本周数据
         */
        private String week;

        /**
         * 本月数据
         */
        private String month;
    }
}
