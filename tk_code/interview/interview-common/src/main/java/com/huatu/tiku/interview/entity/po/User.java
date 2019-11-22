package com.huatu.tiku.interview.entity.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author zhouwei
 * @Description: 学员信息表
 * @create 2018-01-05 下午4:24
 **/

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="t_user")
@Entity
public class User  extends BaseEntity implements Serializable{

    //status 1关注 -1取关
    //bizStatus 0未绑定手机号  1已绑定手机号


    //学员姓名
    private String userName;
    //微信openID
    private String openId;
    //学员姓名
    private String name;
    //手机号
    private String phone;
    //身份证
    private String idCard;
    //民族
    private String nation;
    //性别 （1男 2女）
    private Integer sex;
    //是否处于孕期，哺乳期 (1是  0否)
    private Boolean pregnancy;
    //紧急联系人
    private String keyContact;
    //是否签署协议
    private Boolean agreement;
    //所属地区
    private long areaId;
    //课程名称
    private String classTitle;
}
