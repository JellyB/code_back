package com.huatu.tiku.request.question.v1;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by huangqp on 2018\5\10 0010.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsertQuestionReqV1 extends QuestionReqV1 {
    /**
     * 试题id(数据迁移时使用)
     */
    private Long id;
    /**
     * 创建者
     */
    private Long creatorId;
}
