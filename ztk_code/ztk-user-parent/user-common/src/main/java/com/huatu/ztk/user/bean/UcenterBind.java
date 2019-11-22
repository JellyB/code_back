package com.huatu.ztk.user.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * ucenter绑定表
 * Created by linkang on 7/11/16.
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class UcenterBind implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;	//主键
    private int userid;	//用户id
    private String username;	//用户名
    private String phone;	//手机号
    private String email;	//邮箱
    private String bd;	//(0 手机未绑定 1手机已绑定),(0 邮箱未绑定 1邮箱已绑定)
}
