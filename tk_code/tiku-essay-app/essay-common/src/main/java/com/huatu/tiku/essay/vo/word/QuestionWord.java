package com.huatu.tiku.essay.vo.word;

import com.huatu.tiku.essay.vo.admin.AdminQuestionDeductRuleVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author ZhenYang
 * @Date Created in 2018/2/28 21:18
 * @Description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionWord {
    /* 标题 */
    private String topic;
    /* 试题类型 */
    private String type;
    // 题干
    private String stem;
    // 资料
    private List<Integer> materias;
    //    // 批改得分
//    private String piGai;
    private List<String> deFen;
    private List<String> kouFen;
    /* 参考答案 / 标准答案*/
    private String answerComment;
    /* 阅卷规则 */
    private String rule;
    /* 试题分析 */
    private String analyze;
    /* 材料与标准答案点评 */
    private String remark;
    /* 试题分数 */
    private double score;
    /*  难度系数 */
    private double difficultGrade;
    /* 扣分规则*/
    private AdminQuestionDeductRuleVO questionDeductRuleVO = new AdminQuestionDeductRuleVO();
}
