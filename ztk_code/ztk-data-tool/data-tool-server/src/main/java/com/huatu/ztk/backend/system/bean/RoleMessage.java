package com.huatu.ztk.backend.system.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Author: xuhuiqiang
 * Time: 2016-12-04  17:01 .
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
//角色的详细信息
public class RoleMessage {
    private int id;
    private String name;//角色名字
    private String intro;//角色描述
    private String creatTime;//创建时间
    private String creater;//创建者
    private String updateTime;//修改时间
    private String updateer;//修改者
    private int status;//角色状态，1表示该角色正常，0为该角色被锁定，不能被选择，对菜单的权限无效
    private int isbelong=1;//用户是否属于某角色，是为1，否为0
}
