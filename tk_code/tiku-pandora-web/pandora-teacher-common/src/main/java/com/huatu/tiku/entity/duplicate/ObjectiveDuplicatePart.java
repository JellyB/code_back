package com.huatu.tiku.entity.duplicate;

import com.huatu.common.bean.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * Created by huangqp on 2018\5\16 0016.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "question_duplicate_objective")
public class ObjectiveDuplicatePart extends BaseEntity {
    /**
     * 题型
     * 复用的数据只能在一种题型中使用，而且为了查询缩减范围，需要使用题型作为查询条件进行查询，
     * 而复用数据跟试题时一对多实现的，使用试题本身的题型多有不便，所以冗余一份题型在复用数据表中
     */
    private Integer questionType;
    /**
     * 题干
     */
    private String stem;
    /**
     * 题干筛选字段
     */
    private String stemFilter;
    /**
     * 答案
     */
    private String answer;
    /**
     * 解析
     */
    private String analysis;
    /**
     * 解析筛选字段
     */
    private String analysisFilter;
    /**
     * 选项
     */
    private String choices;
    /**
     * 选项筛选字段
     */
    private String choicesFilter;
    /**
     * 扩展
     */
    private String extend;
    /**
     * 扩展筛选字段
     */
    private String extendFilter;
    /**
     * 判断依据
     */
    private String judgeBasis;
    /**
     * 判断依据筛选字段
     */
    private String judgeBasisFilter;
}
