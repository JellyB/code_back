package com.huatu.tiku.essay.vo.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsSingleResultVO {

    /**
     * 单题id
     */
    private Long id;
    /**
     * 地区名字
     */
    private String areaName;
    /**
     * 作答次数
     */
    private Long answerNum;
    /**
     * 下载次数
     */
    private Integer downloadNum;
    /**
     * 批改次数
     */
    private Long correctNum;
    /**
     * 最高得分
     */
    private Double maxScore;
    /**
     * 最低得分
     */
    private Double minScore;
    /**
     * 平均分
     */
    private Double averageScore;
    /**
     * 平均分
     */
    private Long areaId;
    /**
     * 单题id
     */
    private Long questionId;
    /**
     * 套题组
     */
    private List<StatisticsSingleResultVO> statisticsSingleResultVOS;
}
