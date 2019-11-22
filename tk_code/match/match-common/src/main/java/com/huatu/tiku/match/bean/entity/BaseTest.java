package com.huatu.tiku.match.bean.entity;

import com.huatu.common.bean.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;
import javax.persistence.Transient;
import java.sql.Timestamp;

/**
 * 基础测试，此处使用 pandora.area 表
 * Created by lijun on 2018/10/11
 */
@Table(name = "area")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class BaseTest extends BaseEntity {

    private String name;
    private Long pId;
    private Integer sort;

    /**
     * 虚拟列的声明方式
     */
    @Transient
    private String fullName;

    /**
     * 如果 需要使用 @Builder 注解 必须手动声明构造函数，原因 为 lombok 组件在处理继承中有问题
     */
    @Builder
    public BaseTest(Long id, Integer bizStatus, Integer status, Long creatorId, Timestamp gmtCreate, Long modifierId, Timestamp gmtModify, String name, Long pId, Integer sort) {
        super(id, bizStatus, status, creatorId, gmtCreate, modifierId, gmtModify);
        this.name = name;
        this.pId = pId;
        this.sort = sort;
    }
}
