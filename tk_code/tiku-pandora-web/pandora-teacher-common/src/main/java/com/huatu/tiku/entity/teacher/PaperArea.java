package com.huatu.tiku.entity.teacher;

import com.huatu.common.bean.BaseEntity;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * Created by huangqp on 2018\6\23 0023.
 */
@NoArgsConstructor
@Data
@Table(name = "paper_area")
public class PaperArea extends BaseEntity{
    /**
     * 地区id
     */
    private Long areaId;

    /**
     * 试卷id
     */
    private Long paperId;

    /**
     * 试卷类型（1实体卷2活动卷）
     */
    private Integer paperType;

    @Builder
    public PaperArea(Long id, Integer bizStatus, Integer status, Long creatorId, Timestamp gmtCreate, Long modifierId, Timestamp gmtModify, Long areaId, Long paperId, Integer paperType) {
        super(id, bizStatus, status, creatorId, gmtCreate, modifierId, gmtModify);
        this.areaId = areaId;
        this.paperId = paperId;
        this.paperType = paperType;
    }
}

