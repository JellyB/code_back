package com.huatu.ztk.paper.bean;


import com.huatu.ztk.knowledge.bean.Module;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 练习卷
 * Created by shaojieyue
 * Created time 2016-04-29 14:46
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class PracticePaper implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name; //试卷名字
    private int qcount;//题量
    private double difficulty;//难度系数
    private int catgory;//考试科目
    private int subject;//知识点类目
    private List<Module> modules;//模块列表
    private List<Integer> questions;
}
