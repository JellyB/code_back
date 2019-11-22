package com.huatu.tiku.interview.entity.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by x6 on 2018/4/12.
 * 模块练习
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="t_module_practice")
public class ModulePractice  extends BaseEntity {
    //---------答题日期---------
    private String answerDate;
    // -----姓名---------
    private String name;
    //---------openId---------
    private String openId;
    //----------练习内容(1自我认知  2工作实务   3策划组织  4综合分析   5材料题与特殊题型   6套题演练)-------------
    private Long practiceContent;

    //题目名称
    private String paperName;
    //语音语调
    private String pronunciation;
    //流畅程度
    private String fluencyDegree;
    //仪态动作
    private String deportment;

    //优点
    private String advantage;

    //问题
    private String disAdvantage;

    //其他点评
    private String elseRemark;

    //综合评价
    private String overAllRemark;

}
