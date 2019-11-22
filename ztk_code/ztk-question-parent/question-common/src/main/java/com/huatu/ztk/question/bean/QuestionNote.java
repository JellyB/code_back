package com.huatu.ztk.question.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * 试题笔记
 * Created by shaojieyue on 5/3/16.
 */

@Document(collection = "ztk_question_note")
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class QuestionNote  implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private int id;//id
    private int userId;//用户id
    private int questionId;//问题id
    private String content;//笔记内容
    private long createTime;
}
