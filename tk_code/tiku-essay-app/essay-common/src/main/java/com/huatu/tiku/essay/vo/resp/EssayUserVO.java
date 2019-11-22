package com.huatu.tiku.essay.vo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: ZhenYang
 * @Date: Created in 2018/1/7 21:29
 * @Modefied By:
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class EssayUserVO {
    private Integer userId;
    private Object mobile;
    private String areaName;
    private Double examScore;
    private Integer spendTime;
}
