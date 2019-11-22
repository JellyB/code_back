package com.huatu.ztk.paper.vo;

import com.huatu.ztk.paper.dto.ScoreRankDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author shanjigang
 * @date 2019/3/7 11:17
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class PeriodTestRankVo {
    /**
     * 排名前十的考生
     */
    private List<ScoreRankDto> comprehensiveRank;

    /**
     * 自己
     */
    private ScoreRankDto self;
}
