package com.huatu.ztk.report.bean;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * 试题总结
 * Created by shaojieyue
 * Created time 2016-05-27 09:22
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
@Document(collection = "user_question_summary")
public class QuestionSummary implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    private String id;
    private long uid;//用户id
    private int subject;//科目
    private int acount;//试题个数,不重复 类似mysql的distinct
    private int rcount;//正确个数,不重复 类似mysql的distinct
    private int wcount;//错误个数,不重复 类似mysql的distinct
    private int asum;//所有做过的题次数(带重复的)
    private int rsum;//所有做过正确的的题次数(带重复的)
    private int wsum;//所有做过错误的题次数(带重复的)
    private int times;//做题耗时
    private int speed;//答题速度
    private double accuracy;//正确率

}