package com.huatu.tiku.entity.knowledge;

import com.huatu.common.bean.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * 知识点表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "knowledge")
public class Knowledge extends BaseEntity {
    /**
     * 知识点名称
     */
    private String name;
    /**
     * 上级知识点id
     */
    private Long parentId;
    /**
     * 知识点层级
     */
    private Integer level;
    /**
     * 是否是叶子结点
     */
    private Boolean isLeaf;
    /**
     * 知识点顺序
     */
    private Integer sortNum;
    @Builder

    public Knowledge(Long id, Integer bizStatus, Integer status, Long creatorId, Timestamp gmtCreate, Long modifierId, Timestamp gmtModify, String name, Long parentId, Integer level, Boolean isLeaf, Integer sortNum) {
        super(id, bizStatus, status, creatorId, gmtCreate, modifierId, gmtModify);
        this.name = name;
        this.parentId = parentId;
        this.level = level;
        this.isLeaf = isLeaf;
        this.sortNum = sortNum;
    }
}
