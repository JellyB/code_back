package com.huatu.tiku.essay.entity;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Created by duanxiangchao on 2019/7/17
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "v_essay_base_area")
@EqualsAndHashCode(callSuper = false)
@Builder
@DynamicUpdate
@DynamicInsert
public class EssayBaseArea extends BaseEntity implements Serializable {

    private String code;

    private String name;

    private Long parentId;

    private Integer sorting;

    private Integer type;

    private Long provinceId;

}
