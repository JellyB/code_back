package com.huatu.tiku.entity.common;

import com.huatu.common.bean.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * Created by x6 on 2018/5/8.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "area")
public class Area extends BaseEntity {
    /**
     * 名称
     */
    private String name;
    /**
     * id
     */
    private Long pId;
    /**
     *  优先级
     */
    private Integer sort;
    @Builder
    public Area(Long id, Integer bizStatus, Integer status, Long creatorId, Timestamp gmtCreate, Long modifierId, Timestamp gmtModify, String name, Long pId, Integer sort) {
        super(id, bizStatus, status, creatorId, gmtCreate, modifierId, gmtModify);
        this.name = name;
        this.pId = pId;
        this.sort = sort;
    }
}
