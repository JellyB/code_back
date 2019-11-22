package com.huatu.tiku.entity.common;


import com.huatu.common.bean.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.Table;

/**
 * @author jbzm
 * @date Create on 2018/3/9 10:30
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@Table(name = "t_user")
public class User extends BaseEntity {


    private String username;
    private String password;

}