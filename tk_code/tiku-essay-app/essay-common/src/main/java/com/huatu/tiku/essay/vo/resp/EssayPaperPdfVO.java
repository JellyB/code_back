package com.huatu.tiku.essay.vo.resp;

import com.huatu.tiku.essay.entity.EssayMaterial;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Create by jbzm on 171214
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EssayPaperPdfVO {
    //试卷名字
    private String name;
    //试卷地点
    private String areaName;
    //总分
    private double score;
    //答题时限
    private int limitTime;
    //试卷内容
    private List<EssayMaterial> content;

}
