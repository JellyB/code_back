package com.huatu.tiku.request.question.v1;

import com.huatu.tiku.entity.common.TeacherAreaYear;
import com.huatu.tiku.request.BaseReq;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by huangqp on 2018\7\9 0009.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionReqV1 extends BaseReq {
    /**
     * 复用id（复用数据id,使用已有数据）
     */
    private Long duplicateId;
    /**
     * 是否是有用题(1有用2废弃)
     */
    private Integer availFlag;
    /**
     * 缺失题标识
     */
    private Integer missFlag;
    /**
     * 试题实际类型
     * 99单选题100多选题101不定项题109判断题
     * 105复合题106主观题107复合主观题
     * 110名词解释111问答题
     */
    @NotNull(message = "题型不能为空")
    private Integer questionType;
    /**
     * 复合题Id
     */
    private Long multiId;
    /**
     * 难度
     */
    private Integer difficultyLevel;
    /**
     * 学科
     */
    @NotNull(message = "学科不能为空")
    private Long subject;
    /**
     * 学段(教综学段可以为空)
     */
    private List<Long> grades;
    /**
     * 知识点id
     */
    private List<Long> knowledgeIds;
    /**
     * 标签
     */
    private List<Long> tags;
    /**
     * 时间年份组合
     */
    private List<TeacherAreaYear> questionAreaYears;
    /**
     * 逻辑状态
     */
    private Integer status;
    /**
     * 材料id
     */
    private List<Long> materialIds;


    /**
     * 试题统计标签
     */
    private List<String> statisticsTagList;

}

