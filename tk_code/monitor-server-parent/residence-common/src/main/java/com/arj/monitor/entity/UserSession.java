package com.arj.monitor.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * @author zhouwei
 * @create 2018-10-17 下午12:36
 **/
//@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
//@Table(name="police_token")
public class UserSession implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    private  Long userId;
    /**
     * 用户类型 0 普通用户 1 管理员
     */
    private  int userType;

    private String token;
    /**
     * 有效期
     * */
    private Long expireTime;
}
