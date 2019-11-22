package com.huatu.ztk.backend.system.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2016-12-06  15:46 .
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
//方便角色修改、新增设置该数据结构
public class RoleMenu {
    private RoleMessage role;
    private List<Menu> menuList;//角色对应能放访问的菜单列表
}
