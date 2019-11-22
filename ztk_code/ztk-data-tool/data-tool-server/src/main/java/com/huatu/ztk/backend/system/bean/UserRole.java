package com.huatu.ztk.backend.system.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2016-12-05  20:23 .
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class UserRole {
    private UserMessage userMessage;//用户信息
    private List<RoleMessage> allRoles;//系统所有角色,及用户是否是该角色
}
