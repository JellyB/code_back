package com.huatu.ztk.report.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * 练习总结
 * Created by shaojieyue
 * Created time 2016-05-27 09:35
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
@Document(collection = "ztk_practice_summay")
public class PracticeSummary implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    private String id;//uid+subject+yyyyMM 整个汇总id=uid+subject+-1
    private long uid;//用户id
    private int subject;//科目
    private int practiceCount;//练习次数
    private int dayCount;//练习天数
    private double average;//练习平均值 xx次/天
    private int times;//做题耗时 单位:s
    private int rcount;//本月正确题数
    private int wcount;//本月错误题数
    private int speed;//平均答题速度 单位:s
}
