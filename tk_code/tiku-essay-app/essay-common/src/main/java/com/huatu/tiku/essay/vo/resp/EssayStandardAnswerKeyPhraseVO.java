package com.huatu.tiku.essay.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by huangqp on 2017\12\6 0006.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EssayStandardAnswerKeyPhraseVO {
    private long id;
    private String item;
    //关键短句该出现的位置，1.全篇（默认）2.开头3.开头或中间4.中间5.中间或结尾6.结尾
    private int  position;
    //关键短句分数
    private double score;
    //对应试题id
    private long questionDetailId;
    //关键句类型:1为应用文关键句，2为议论文中心思想，3为议论文主题
    private int type;
    private int bizStatus = 0;
    private int status  = 1;
    //关键词列表
    private List<EssayStandardAnswerKeyWordVO> keyWordVOList;

    //上级id（如果是近似句，该字段为对应的关键句id，否则为0）
    private long pid;
    //论点划档级别（1 一档 2 二档）
    private int level;
    //关键句的近似句
    private List<EssayStandardAnswerKeyPhraseVO> similarPhraseList;
}
