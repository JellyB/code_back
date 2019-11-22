package com.huatu.ztk.paper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author shanjigang
 * @date 2019/3/5 14:56
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class SocreDistributionDto {
    /**
     * 击败比例
     */
    private int beatRatio;

    /**
     * 得分人数
     */
    private int count;

    /**
     * 是否是自己得分
     */
    private Boolean isSelf;

    /**
     * 分数
     */
    private int score;
}
