package com.huatu.tiku.response.question.v1;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by huangqp on 2018\5\10 0010.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SelectCompositeQuestionRespV1 extends SelectQuestionRespV1{
    /**
     * 总括要求
     */
    private String omnibusRequirements;
    /**
     * 子题部分
     */
    private List<SelectQuestionRespV1> children;
}
