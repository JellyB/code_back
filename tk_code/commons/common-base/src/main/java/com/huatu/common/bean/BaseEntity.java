package com.huatu.common.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 基础类文件
 * Created by lijun on 2018/5/28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseEntity implements Serializable {

    //此处官方建议 @KeySql(dialect = IdentityDialect.MYSQL) 取回主键
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 业务状态
     */
    private Integer bizStatus;

    /**
     * 逻辑状态
     */
    private Integer status;

    /**
     * 创建者
     */
    private Long creatorId;

    /**
     * 创建时间
     */
    private Timestamp gmtCreate;

    /**
     * 修改者
     */
    private Long modifierId;

    /**
     * 修改时间
     */
    private Timestamp gmtModify;

}
