package com.huatu.ztk.user.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * ucenter用户信息表
 * Created by linkang on 7/11/16.
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class UcenterMember implements Serializable {
    private static final long serialVersionUID = 1L;

    private int uid;
    private String username;
    private String password;
    private String email;
    private String myid;
    private String myidkey;
    private String regip;
    private int regdate;    //System.currentTimeMillis()/1000
    private int lastloginip;
    private int lastlogintime;
    private String salt;
    private String secques;
    private int appid; //砖题库-6 华图教育+-55 教师网-56
    private int credit;
}
