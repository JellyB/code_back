package com.huatu.tiku.interview.entity.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * @Author jbzm
 * @Date Create on 2018/1/17 17:11
 */
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Table(name = "t_sign_in")
public class SignIn extends BaseEntity {

    /**
     * 签到时间
     */
    private Date signTime;
    /**
     * 用户openId
     */
    private String openId;

}
