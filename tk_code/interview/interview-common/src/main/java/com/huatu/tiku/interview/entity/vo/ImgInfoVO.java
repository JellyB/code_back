package com.huatu.tiku.interview.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Author: xuhuiqiang
 * Time: 2017-04-18  14:27 .
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class ImgInfoVO {
    private String imgsrc = "";//截取到<img>标签中src之后的所有内容
    private String base = "";//64位base码
    private int width = -1;//图片宽度
    private int height = -1;//图片高度
}
