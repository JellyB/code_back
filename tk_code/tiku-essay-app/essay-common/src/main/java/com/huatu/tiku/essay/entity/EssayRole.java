package com.huatu.tiku.essay.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

/**
 * @author zhaoxi
 * @Description: 用户角色
 * @date 2018/10/17上午11:00
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "v_essay_role")
@EqualsAndHashCode(callSuper = false)
@DynamicUpdate
@DynamicInsert
public class EssayRole  extends BaseEntity  implements Serializable {
    //角色名称
    private String roleName;

}
