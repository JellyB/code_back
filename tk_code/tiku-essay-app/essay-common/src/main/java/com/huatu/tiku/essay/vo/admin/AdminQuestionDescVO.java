package com.huatu.tiku.essay.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 描述
 */
@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
public class AdminQuestionDescVO {


    //描述
    private String item;
    //描述分数
    private double score;




}
