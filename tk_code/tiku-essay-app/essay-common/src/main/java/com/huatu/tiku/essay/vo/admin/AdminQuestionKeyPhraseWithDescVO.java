package com.huatu.tiku.essay.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 有描述的关键句
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminQuestionKeyPhraseWithDescVO {

    private long id;
    private String item;
    //关键短句该出现的位置，1为出现在首段，2为出现在中间，3为出现在末尾4为全篇(默认2)
    private int  position;
    //关键短句分数
    private double score;
    //对应试题id
    private long questionDetailId;

    private int type;
    //关键句
    private List<AdminQuestionKeyPhraseVO> keyPhraseList;

}
