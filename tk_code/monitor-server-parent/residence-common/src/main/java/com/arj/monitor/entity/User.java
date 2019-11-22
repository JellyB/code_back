package com.arj.monitor.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author zhouwei
 * @Description: 用户实体类
 * @create 2018-10-15 上午10:48
 **/
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@Table(name = "police_user")
public class User extends BaseEntity {
    /**
     * 账号
     */
    private String userName;
    /**
     * 真实姓名
     */
    private String realName;
    /**
     * 性别
     */
    private int sex;
    /**
     * 年龄
     */
    private int age;
    /**
     * 出生日期
     */
    private Date birthday;
    /**
     * 密码
     */
    private String password;
    /**
     * 手机号
     */
    private String telephone;
    /**
     * 头像
     */
    private String avatar;
    /**
     * 证件号
     */
    private String cardId;
    /**
     * 证件类型
     * 1、身份证 2、军官证 3、护照 4、台湾通行证 5、港澳通行证
     */
    private int cardType;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 区域id
     */
    private Long areaId;
    /**
     * 详细地址
     */
    private String detailAddress;
    /**
     * 微信号
     */
    private String weChatNumber;
}
