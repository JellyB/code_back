package com.huatu.tiku.interview.entity.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by x6 on 2018/4/12.
 * 全真模考
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="t_mock_practice")
public class MockPractice   extends BaseEntity {
    //---------答题日期---------
    private String answerDate;
    // -----姓名---------
    private String name;
//    //---------用户ID---------
//    private Long userId;
    //---------openId---------
    private String openId;

    //试卷id
    private long paperId;

    //语音语调
    private String pronunciation;
    //流畅程度
    private String fluencyDegree;
    //仪态动作
    private String deportment;

    //其他评价
    private String elseRemark;
    //综合评分
    private Double overAllScore;
}
