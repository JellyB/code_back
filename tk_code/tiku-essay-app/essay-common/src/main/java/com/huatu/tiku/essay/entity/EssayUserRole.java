package com.huatu.tiku.essay.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * @author zhaoxi
 * @Description: 用户-角色关系
 * @date 2018/10/17上午11:00
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "v_essay_user_role")
@EqualsAndHashCode(callSuper = false)
@DynamicUpdate
@DynamicInsert
public class EssayUserRole extends BaseEntity{

    //用户id
    private Long userId;
    //角色id
    private Long roleId;
    //角色名称
    private String roleName;
    //描述
    private String descrp;

}
