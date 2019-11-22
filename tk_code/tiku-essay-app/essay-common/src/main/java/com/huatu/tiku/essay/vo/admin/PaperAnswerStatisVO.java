package com.huatu.tiku.essay.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by x6 on 2018/4/9.
 * 数据统计SQL返回vo
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PaperAnswerStatisVO {

    private Double max;
    private Double min;
    private Double avg;
    private Long count;

}
