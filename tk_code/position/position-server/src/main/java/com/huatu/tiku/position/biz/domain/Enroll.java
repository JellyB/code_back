package com.huatu.tiku.position.biz.domain;

import com.huatu.tiku.position.base.domain.BaseDomain;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/** 意向报名记录
 * @author wangjian
 **/
@Data
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames={"userId", "positionId"})})
public class Enroll extends BaseDomain {

    private static final long serialVersionUID = 535353995896191669L;

    private Long userId;//用户id

    private Long positionId;//职位id

    /**
     * 数据状态 0 删除 1 正常
     */
    @Column(columnDefinition = "tinyint DEFAULT 1")
    private Byte status;
}
