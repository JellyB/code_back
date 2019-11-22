package com.huatu.ztk.backend.system.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2017-01-16  17:42 .
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class OperateMenu {
    private Operate operate;//操作信息
    private List<Menu> menus;//菜单信息
}
