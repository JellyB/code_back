package com.huatu.tiku.entity.cop;

import com.huatu.common.bean.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;

/**
 * @author zhaoxi
 * @Description: 招警机考-院校
 * @date 2018/8/17下午3:33
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "school")
public class School extends BaseEntity {

    /**
     * 所属地区
     */
    private Long areaId;
    private String areaName;


    /**
     * 院校名称
     */
    private String name;
}
