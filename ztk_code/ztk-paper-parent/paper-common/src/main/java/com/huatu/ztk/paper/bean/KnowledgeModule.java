package com.huatu.ztk.paper.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 每日特训可配置知识点
 * Created by shaojieyue
 * Created time 2016-05-20 16:47
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class KnowledgeModule implements Serializable {
    private static final long serialVersionUID = 1L;

    private int pointId;
    private String name;
}
