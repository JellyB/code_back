package com.huatu.ztk.question.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

/**
 *
 * 用户作答试题记录
 * Created by shaojieyue
 * Created time 2016-09-08 11:41
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
@Document
public class QuestionRecord  implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Getter(onMethod = @__({ @JsonIgnore}))
    private String id;
    @Getter(onMethod = @__({ @JsonIgnore }))
    private long uid;//用户id
    @Getter(onMethod = @__({ @JsonIgnore }))
    private int qid;//试题id
    private List<Integer> answers;//用户作答答案列表
    private List<Integer> times;//用户作答耗时列表

}
