package com.huatu.ztk.backend.system.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2016-11-23  17:41 .
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class UserMessage {
    private int id;
    private String uname;//用户账号
    private String account;//用户真实姓名
    private int status;//用户状态
    private String password;//用户密码
    private String lastLoginIp;//最后登录IP
    private String lastLoginTime;//最后登录时间
    private int loginSuccessCount;//登录成功次数
    private String creatTime;//用户创建时间
    private String creator;//创建该用户的管理员
    private int creatorId;//创建该用户的管理员
    private String updateTime;//用户信息修改时间
    private String updateer;//修改该用户信息的管理员
}

