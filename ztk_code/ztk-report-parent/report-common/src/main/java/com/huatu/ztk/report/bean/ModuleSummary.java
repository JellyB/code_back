package com.huatu.ztk.report.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户模块统计
 * Created by shaojieyue
 * Created time 2016-06-20 15:46
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class ModuleSummary implements Serializable{
    private static final long serialVersionUID = 1L;
    private long uid;//用户id
    private int subject;//科目
    private int moduleId;//模块id
    private String moduleName;//模块名称
    private int acount;//已做题数
    private int rcount;//正确题数
    private int wrong;//错误题数
    private int score;//个人预测分
    
}
