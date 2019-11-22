package com.huatu.tiku.entity.tag;

import com.huatu.common.bean.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Table;

/**
 * @author zhouwei
 * @Description:
 * @create 2018-04-25 下午1:28
 **/
@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
@Table(name = "tag")
public class Tag extends BaseEntity {
    /**
     * 标签名称
     */
    private String name;
    /**
     * 标签描述
     */
    private String description;
    /**
     * 标签渠道  1教师2学员
     */
    private Integer channel;
}
