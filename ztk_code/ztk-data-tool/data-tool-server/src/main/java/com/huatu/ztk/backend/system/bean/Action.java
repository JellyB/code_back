package com.huatu.ztk.backend.system.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2017-04-22  19:39 .
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class Action {
    private int id;
    private String name;//功能名称
    private String discription;//描述
    private int parentId;//父级id，若为0，表示为顶级功能
    private String parentName;//父级名字
    private int status;//操作状态，1为正常，0为删除
    private int isBelong;//用于标记某角色是否有该该菜单的权限，有为1，无为0；
    private List<Action> subAction;//子级功能
}
