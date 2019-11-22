package com.huatu.tiku.essay.vo.resp;

import com.huatu.tiku.essay.entity.EssayMaterial;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Create by jbzm on 171213
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EssayQuestionPdfVO {
    //题干内容内容
    private String stem;
    //地区名称
    private String areaName;
    //考试时间
    private int limitTime;
    //答题要求
    private String answerRequire;
    //最多字数
    private int inputWordNumMax;
    //资料集合
    private List<EssayMaterial> contensList;
}
