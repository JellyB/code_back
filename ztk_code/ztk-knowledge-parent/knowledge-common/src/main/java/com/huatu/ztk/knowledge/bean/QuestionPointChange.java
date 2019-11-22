package com.huatu.ztk.knowledge.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by lenovo on 2017/10/1.
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class QuestionPointChange {
    private int id;
    private int questionId;
    private int newPointId;
    private int oldPointId;
    private int subject;
    private int level;
}
