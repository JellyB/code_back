package com.huatu.tiku.entity.subject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;

/**
 * 考试类型表
 * Created by huangqingpeng on 2018/8/24.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "v_new_catgory")
public class Category {
    /**
     * ID
     */
    private Integer id;

    /**
     * 名称
     */
    private String name;

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
