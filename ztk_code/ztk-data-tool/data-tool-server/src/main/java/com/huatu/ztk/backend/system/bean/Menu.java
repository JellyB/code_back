package com.huatu.ztk.backend.system.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shaojieyue
 * Created time 2016-11-16 16:18
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class Menu {
    private int id=0;
    private String text;//菜单名字
    private int parentId;//上级菜单Id，若为0，则为顶级菜单
    private String parentName;//上级菜单名
    private String intro;//菜单描述
    private String sref;//菜单对应的angular链接
    private String templateUrl;//菜单对应的绝对地址
    private int isbelong;//用于标记某角色是否有该该菜单的权限，有为1，无为0；
    private String creatTime;//创建时间
    private String creater;//创建者
    private String updateTime;//修改时间
    private String updateer;//修改者
    private int status;//菜单状态，1为正常，0为已经删除
    private List<Menu> subMenu= new ArrayList<>();//子菜单
}
