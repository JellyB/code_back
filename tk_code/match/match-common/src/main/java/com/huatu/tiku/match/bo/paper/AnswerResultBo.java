package com.huatu.tiku.match.bo.paper;

import lombok.Data;

/**
 * Created by lijun on 2019/1/3
 */
@Data
public class AnswerResultBo {

    /**
     * 试题ID
     */
    private Integer questionId;

    /**
     * 用户答案
     */
    private String answer;

    /**
     * 总耗时
     */
    private Integer expireTime;

    /**
     * 是否有疑问
     */
    private Integer doubt;

    /**
     * 是否正确
     */
    private Integer correct;
}
