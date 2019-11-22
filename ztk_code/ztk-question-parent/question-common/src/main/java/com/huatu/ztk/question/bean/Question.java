
package com.huatu.ztk.question.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

/**
 * 试题实体
 * Created by shaojieyue on 4/18/16.
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Document(collection = "ztk_question")
public abstract class Question implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private int id;//id
    private int type;//题型: 单选,多选,对错,复合题
    private String from;//来源
    private String material;//材料
    private int year;//试题年份
    private int area;//试题区域
    @Getter(onMethod = @__({ @JsonIgnore }))
    private int status;//状态 BB102 审核标示,有效标识
    @Getter(onMethod = @__({ @JsonIgnore }))
    private long createTime;//创建时间
    @Getter(onMethod = @__({ @JsonIgnore }))
    private long createBy;//创建人
    @Getter(onMethod = @__({ @JsonIgnore }))
    private long updateTime;//更新时间
    @Getter(onMethod = @__({ @JsonIgnore }))
    private long updateBy;//更新人
    @Getter(onMethod = @__({ @JsonIgnore }))
    private long checker;//线下审核人
    private int mode;//题的模式 如：真题，模拟题
    private int subject;//科目
    private List<String> materials;//材料列表
    private String teachType;//教研题型
    private int difficult;//难度系数
    private float score;//分数
    private int channel; //录题渠道 如：1或者null标准录题;2试卷导入;3散题导入;4cass数据迁移
    @Transient //映射忽略的字段，该字段不会保存到mongodb
    private List<Question> childrens;
    //多知识点存储
    private List<KnowledgeInfo> pointList;
}
