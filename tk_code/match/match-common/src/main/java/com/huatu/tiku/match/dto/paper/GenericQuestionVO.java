package com.huatu.tiku.match.dto.paper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * 客观题VO，单选、多选、对错
 * Created by lijun on 2018/11/1
 */
@Builder
@Data
@AllArgsConstructor
public class GenericQuestionVO {

    /**
     * id
     */
    private Integer id;

    /**
     * 答案
     */
    private int answer;
}
