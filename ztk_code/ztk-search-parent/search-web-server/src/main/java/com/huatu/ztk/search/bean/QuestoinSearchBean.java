package com.huatu.ztk.search.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by shaojieyue
 * Created time 2016-05-04 16:44
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestoinSearchBean {
    private int id;//问题id
    private String fragment;//查询出带有高亮的片段
    private String from;//来源
    private int type;//试题类型
    /**
     * 试题材料内容
     */
    private String material;
}
