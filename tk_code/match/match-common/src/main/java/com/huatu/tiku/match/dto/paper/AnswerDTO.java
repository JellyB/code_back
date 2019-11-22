package com.huatu.tiku.match.dto.paper;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by lijun on 2018/11/1
 */
@Data
public class AnswerDTO implements Serializable {
    /**
     * 试题ID
     */
    private Integer questionId;

    /**
     * 答案
     */
    private String answer;

    /**
     * 耗时
     */
    private Integer expireTime;

    /**
     * 是否有疑问
     */
    private Integer doubt;
}
