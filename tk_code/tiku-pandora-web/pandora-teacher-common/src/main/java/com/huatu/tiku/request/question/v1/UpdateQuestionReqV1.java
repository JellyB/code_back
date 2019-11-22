package com.huatu.tiku.request.question.v1;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * Created by huangqp on 2018\5\10 0010.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateQuestionReqV1 extends QuestionReqV1{
    @NotNull(message = "试题id不能为空")
    /**
     * 试题id(数据迁移时使用)
     */
    private Long id;
    /**
     * 修改者
     */
    private Long modifierId = 0L;
}
