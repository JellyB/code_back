package com.huatu.tiku.essay.vo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EssayStatisticsPaperVO {
    /**
     * 套题id
     */
    private Long id;
    /**
     * 试卷名称
     */
    private String name;
    /**
     * 判断是模考还是套题
     */
    private Integer type;
    /**
     * 总分
     */
    private Double score;
    /**
     * 试题年份
     */
    private String paperYear;
    /**
     * 地区名称
     */
    private String areaName;
}
