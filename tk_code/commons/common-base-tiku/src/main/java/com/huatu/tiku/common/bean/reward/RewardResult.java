package com.huatu.tiku.common.bean.reward;

import lombok.Builder;
import lombok.Data;

/**
 * @author hanchao
 * @date 2017/10/16 13:06
 */
@Data
@Builder
public class RewardResult {
    private int gold;
    private int experience;
    private boolean completed;
}
