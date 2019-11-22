package com.huatu.tiku.essay.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 有描述的关键词
 */
@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
public class AdminQuestionKeyWordWithDescVO {

    //id
    private long id;
    //描述
    private String item;
    //描述分数
    private double score;

    private int type;

    //对应试题id
    private long questionDetailId;
    //关键词
    private List<AdminQuestionKeyWordVO> keyWordList;


}
