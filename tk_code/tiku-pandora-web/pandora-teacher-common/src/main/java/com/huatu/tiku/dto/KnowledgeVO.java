package com.huatu.tiku.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by x6 on 2018/5/5.
 * 知识点VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeVO {
    /**
     * 知识点id
     */
    private Long knowledgeId;
    /**
     * 知识点名称
     */
    private String knowledgeName;
    /**
     * 知识点下试题
     */
    private Integer count;
    /**
     * 是否是叶子结点
     */
    private Boolean haveSub = true;
    /**
     * 知识点层级
     */
    private Integer level;
    /**
     * 下级知识点
     */
    private List<KnowledgeVO> knowledgeTrees;
    /**
     * 科目id
     */
    private List<Long> subjectIds;
    /**
     * 知识点
     */
    private List<String> subjectNames;

}
