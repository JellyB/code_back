package com.huatu.tiku.essay.vo.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatisticsSingerleVO {
    /**
     * 单题组id
     */
    private Long id;
    /**
     * 单题结果集合
     */
    private List<StatisticsSingleResultVO> statisticsSingleResultVOS;
    /**
     * 成绩列表集合
     */
    private List<StatisticsSingleGradeVO> statisticsSingleGradeVOS;
}
