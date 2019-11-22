package com.huatu.tiku.interview.entity.po;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @Author jbzm
 * @Date Create on 2018/1/23 10:23
 */
@Data
@Entity
@Table(name = "t_admin")
public class Admin extends BaseEntity implements Serializable {
    private String username;
    private String password;
}
