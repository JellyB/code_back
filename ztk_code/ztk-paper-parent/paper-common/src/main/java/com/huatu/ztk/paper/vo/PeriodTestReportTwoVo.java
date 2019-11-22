package com.huatu.ztk.paper.vo;

import com.huatu.ztk.paper.dto.PointFocusDto;
import com.huatu.ztk.paper.dto.ScoreRankDto;
import com.huatu.ztk.paper.dto.SocreDistributionDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author shanjigang
 * @date 2019/3/51 4:52
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class PeriodTestReportTwoVo {
    /**
     * 需要重点关注列表
     */
    private List<PointFocusDto> focusList;

    /**
     * 成绩排行
     */
    private PeriodTestRankVo scoreTop;

    /**
     * 成绩分布统计信息
     */
    private List<SocreDistributionDto> socreDistribution;
}
