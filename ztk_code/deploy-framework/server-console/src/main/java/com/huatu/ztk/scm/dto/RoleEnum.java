/**
 * Sohu.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.huatu.ztk.scm.dto;

/**
 * 角色
 * @author wenpingliu
 * @version v 0.1 15/9/19 00:30 wenpingliu Exp $$
 */
public enum RoleEnum {
    /**
     * 普通用户，拥有系统赋予权限
     */
    NORMAL_USER(1),
    /**
     * 高级用户，拥有系统权限，且可以发布自己的线上系统
     */
    ADVANCED_USER(2),
    /**
     * 值班用户，拥有所有线上系统维护权限
     */
    DUTY_USER(4),
    /**
     * 运维用户，拥有所有系统部署权限
     */
    OPS_USER(3);

    int roleId;

    private RoleEnum(int roleId) {
        this.roleId = roleId;
    }

}
