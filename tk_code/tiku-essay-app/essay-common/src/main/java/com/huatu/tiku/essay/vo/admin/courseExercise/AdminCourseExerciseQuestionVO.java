package com.huatu.tiku.essay.vo.admin.courseExercise;

import com.huatu.tiku.essay.vo.resp.EssayMaterialVO;
import lombok.*;

import java.util.List;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/8/27
 * @描述 试题列表
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class AdminCourseExerciseQuestionVO {

    /**
     * 试题ID
     */
    private Long questionId;

    /**
     * 试卷ID
     */
    private Long paperId;

    /**
     * 题干
     */
    private String stem;
    /**
     * 训练量
     */
    private Integer answerCount;

    /**
     * 来源
     */
    private String source;

    /**
     * 材料列表
     */
    private List<String> materials;

    /**
     * 题目要求
     */
    private List<String> answerRequire;

    /**
     * 批改价格
     */
    private Double correctPrice;

    /**
     * 已选题目列表需要sort字段
     */
    private Integer sort;

    /**
     * 0 单题 1套题
     */
    private Integer exerciseType;

    private Long id;
}
