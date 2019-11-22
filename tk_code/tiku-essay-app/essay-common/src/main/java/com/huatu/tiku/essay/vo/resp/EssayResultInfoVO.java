package com.huatu.tiku.essay.vo.resp;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author: ZhenYang
 * @Date: Created in 2018/1/7 14:59
 * @Modefied By:
 */
@Data
@NoArgsConstructor
public class EssayResultInfoVO {
    private Integer examCount;
    private Double maxScore;
    private List<String> maxScoreArea;
    private Double avgScore;
    private List<String> avgScoreArea;
    private List<String> examCountMaxArea;
}
