package com.huatu.ztk.backend.system.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2017-01-16  09:53 .
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class Operate {
    private int id;
    private String name;//操作名称
    private String discription;//描述
    private String url;//操作链接地址
    private int menuId;//关联的菜单地址
    private String menuName;//关联菜单名称
    private int status;//操作状态，1为正常，0为删除
}
