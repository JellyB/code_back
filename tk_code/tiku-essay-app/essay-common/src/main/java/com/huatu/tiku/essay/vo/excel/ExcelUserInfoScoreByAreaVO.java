package com.huatu.tiku.essay.vo.excel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: ZhenYang
 * @Date: Created in 2018/1/8 15:32
 * @Modefied By:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExcelUserInfoScoreByAreaVO {
    private int userId;
    private String mobile;
    private String areaName;
    private Double firstScore = 0.0;
    private Double secondScore = 0.0;
    private Double thirdScore = 0.0;
    private Double forthScore = 0.0;
    private Double fifthScore = 0.0;

    private Double score = 0.0;
    private String nick;

}
