package com.huatu.tiku.essay.vo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author ZhenYang
 * @Date Created in 2018/2/6 14:12
 * @Description
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class FeedbackResult {
    private FeedbackDto data;
    private String code;
}
