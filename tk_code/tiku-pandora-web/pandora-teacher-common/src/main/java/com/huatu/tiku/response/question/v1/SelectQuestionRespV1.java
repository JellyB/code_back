package com.huatu.tiku.response.question.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.huatu.tiku.dto.QuestionYearAreaDTO;
import com.huatu.tiku.request.material.MaterialReq;
import com.huatu.tiku.response.BaseResp;
import com.huatu.tiku.response.question.QuestionYearAreaWitchName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by huangqp on 2018\5\10 0010.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SelectQuestionRespV1 extends BaseResp {
    private Long id;
    /**
     * 复用id
     */
    private Long duplicateId;
    /**
     * 真题、模拟题
     */
    private Integer mode = 1;
    /**
     * 试题实际类型
     * 客观类：1单选题2多选题3不定项选择题4选词填空（一边文章多个选项按顺序选择）
     * 判断类：4判断题5辨析题
     * 主观类：6名词解析7简答8论述9教学设计10教育方案设计11作文
     * 连线类：12连线题
     * 复合类：13阅读理解14完形填空15材料分析16案例分析
     */
    private Integer questionType;
    /**
     * 复合题Id
     */
    private Long multiId;
    /**
     * 子题选中的材料id
     */
    private List<Long> materialIds;
    /**
     * 难度
     */
    private Integer difficultyLevel;
    /**
     * 学科
     */
    private Long subject;
    /**
     * 学段(教综学段可以为空)
     */
    private List<Long> grades;
    /**
     * 标签
     */
    private List<Long> tags;
    /**
     * 知识点id
     */
    private List<Long> knowledgeIds;
    /**
     * 业务状态
     */
    private Integer bizStatus;
    /**
     * 材料部分
     */
    private List<MaterialReq> materials;
    /**
     * 是否是有用题(1有用2废弃)
     */
    private Integer availFlag;
    /**
     * 缺失题标识（1缺失2正常）
     */
    private Integer missFlag;
    //**************************下面的属性只为展示
    /**
     * 年份地区（地区名称）
     */
    private QuestionYearAreaDTO questionYearArea;
    /**
     * 难度
     */
    private String difficult;
    /**
     * 知识点(一级-二级-三级)
     */
    private List<String> knowledgeList;
    /**
     * 来源
     */
    private List<String> sourceList;
    /**
     * 标签名称
     */
    private List<String> tagList;
    /**
     * 学段名称
     */
    private List<String> gradeList;
    /**
     * 迁移标识
     */
    private Integer moveFlag;
    /**
     * 题序
     */
    private Integer sort;

    /**
     * 题型名称
     */
    private String typeName;

    private List<QuestionYearAreaWitchName> YearAreaWitchNames;

    /**
     * 统计标签，用于显示
     */
    private List<String> statisticsTagList;

    /**
     * 批量录题解析后的内容回显
     */
    private String parseContent;


}
