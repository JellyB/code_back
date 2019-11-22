package com.huatu.ztk.knowledge.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 模块
 * Created by shaojieyue
 * Created time 2016-05-19 13:28
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class Module implements Serializable {
    private static final long serialVersionUID = 1L;

    private int category;//试卷模块所属类型例如：常识判断，数量关系
    private String name;//所属模块的名字
    private int qcount;//包含题量
}
