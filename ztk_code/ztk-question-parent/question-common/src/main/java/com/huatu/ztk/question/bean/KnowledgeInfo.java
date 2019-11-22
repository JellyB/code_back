package com.huatu.ztk.question.bean;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by huangqingpeng on 2018/8/23.
 */
@Data
@Builder
public class KnowledgeInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Integer> points;//知识点
    private List<String> pointsName;//知识点名称
}
