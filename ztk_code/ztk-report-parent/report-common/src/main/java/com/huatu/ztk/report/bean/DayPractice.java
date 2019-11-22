package com.huatu.ztk.report.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Set;

/**
 * 用户每天练习的简单统计
 * Created by shaojieyue
 * Created time 2016-05-30 18:15
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
@Document(collection = "user_day_practice")
public class DayPractice implements Serializable{
    private static final long serialVersionUID = 1L;
    @Id
    private String id;
    private long uid;//用户id
    private int subject;//科目
    private int count;//练习次数
    private int questionWrongCount;//练习错误题数
    private int questionRightCount;//练习正确题数
    private int questionAllCount;//练习所有题数
    private double difficulty;//本天练习的难度
    private int score;//预测分数
    private String date;//练习时间
    private Set practices;//练习id的结果集
}
