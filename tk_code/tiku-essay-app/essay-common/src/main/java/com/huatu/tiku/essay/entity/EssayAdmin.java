package com.huatu.tiku.essay.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @author zhouwei
 * @Description: 管理员实体类
 * @create 2017-12-13 下午6:48
 **/

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name="v_essay_admin")
@DynamicUpdate
@DynamicInsert
public class EssayAdmin extends BaseEntity implements Serializable{
     private String name;
     private String nickname;
     private String password;
}
