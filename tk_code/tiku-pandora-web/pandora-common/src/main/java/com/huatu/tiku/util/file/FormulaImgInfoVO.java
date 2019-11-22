package com.huatu.tiku.util.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Author: zhaoxi
 * 公式处理
 * Time: 2017-04-18  14:27 .
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class FormulaImgInfoVO {
    private String latex = "";//64位base码
    private float width = -1;//图片宽度
    private float height = -1;//图片高度
}
