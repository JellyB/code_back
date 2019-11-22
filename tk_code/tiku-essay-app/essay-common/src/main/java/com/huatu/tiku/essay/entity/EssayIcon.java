package com.huatu.tiku.essay.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 *  申论首页icon信息
 * Created by x6 on 2018/6/14.
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name="v_essay_icon")
@EqualsAndHashCode(callSuper = false)
@DynamicUpdate
@DynamicInsert
public class EssayIcon extends BaseEntity implements Serializable {

    /**
     * 图片地址
     */
    private String url;
    /**
     * 名称
     */

    private String name;

    /**
     * 序号
     */

    private Integer sort;

    /**
     * 描述
     */
    private String description;

    /**
     * 跳转位置
     */
    private String target;


}
