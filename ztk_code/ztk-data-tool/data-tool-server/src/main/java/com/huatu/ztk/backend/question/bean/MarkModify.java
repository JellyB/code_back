package com.huatu.ztk.backend.question.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.huatu.ztk.question.bean.QuestionMeta;
import lombok.*;

import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2017-03-03  11:17 .
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class MarkModify {
    private int stemMark=0;//题干标记是否修改，0未修改，1为修改，下同(除choicesMark)
    private int answerMark=0;//标准答案
    private List<Integer> choicesMark;//选项，0未修改，1为修改，2为新增，3为删除
    private int analysisMark=0;//解析
    private int scoreMark=0;//分数
    private int difficultMark=0;//难度系数
    private int pointsNameMark=0;//知识点名称
    private int moduleMark=0;//模块
    private int extendMark=0;//拓展
    private int authorMark=0;//作者
    private int reviewerMark=0;//审核人
    private int typeMark=0;//题型: 单选,多选,对错,复合题
    private int materialMark=0;//材料
    private int areaMark=0;//试题区域
    private int requireMark=0;//题目要求
    private int scoreExplainMark=0;//赋分说明
    private int referAnalysisMark=0;//参考解析，作为参考答案
    private int answerRequireMark=0;//答题要求
    private int examPoinMarkt=0;//审题要求
    private int solvingIdeaMark=0;//解题思路
    private int minWordCountMark=0;//最小字数
    private int maxWordCountMark=0;//最大字数
    private int teachTypeMark=0;//教研题型
    private List<Integer> materialsMark;//材料

}
