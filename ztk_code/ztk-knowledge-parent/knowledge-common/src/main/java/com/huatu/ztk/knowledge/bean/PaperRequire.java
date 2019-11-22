package com.huatu.ztk.knowledge.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2017-09-12  15:43 .
 * 试卷要求
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class PaperRequire implements Serializable {
    private static final long serialVersionUID = 1L;

    private int qNum;//试题数量
    private int year;//年份
    private double difficulty;//难度
    private List<Integer> moduleIds;//模块
    private List<Integer> eachTypeCount;//每个类型的题量

}
