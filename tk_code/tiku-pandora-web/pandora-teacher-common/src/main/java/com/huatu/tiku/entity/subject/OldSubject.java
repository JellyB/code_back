package com.huatu.tiku.entity.subject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;

/**
 * vhuatu数据库之前使用的科目表
 * Created by huangqingpeng on 2018/8/24.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "v_new_subject")
public class OldSubject {
    /**
     * ID
     */
    private Integer id;

    /**
     * 名称
     */
    private String name;

    /**
     * 考试类型
     */
    private Integer catgory;

    /**
     * 创建者ID
     */
    private Integer createBy;

    /**
     * 创建时间
     */
    private Integer createTime;

    /**
     * 状态
     */
    private Integer status;
}
