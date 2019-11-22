package com.huatu.ztk.question.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * 试题扩展属性
 * Created by shaojieyue
 * Created time 2017-02-16 17:03
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
@Document(collection = "question_extend")
public class QuestionExtend  implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private int qid;//试题id
    private int paperId;//所属试卷id
    private int moduleId;//模块
    private String extend;//拓展
    private String author;//作者
    private String reviewer;//审核人
    private float sequence;//题序
    private String orgin;//题源
}
