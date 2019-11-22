package com.huatu.ztk.backend.question.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 保存mongo中的临时信息
 * Author: xuhuiqiang
 * Time: 2017-04-12  20:57 .
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Document(collection = "question_id_base")
public class QuestionTemp {
    @Id
    private int id;//id
    private int questionId;
}
