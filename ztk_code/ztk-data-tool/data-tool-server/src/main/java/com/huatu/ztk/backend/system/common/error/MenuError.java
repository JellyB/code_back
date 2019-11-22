package com.huatu.ztk.backend.system.common.error;

import com.huatu.ztk.commons.exception.ErrorResult;

/**
 * Author: xuhuiqiang
 * Time: 2016-12-08  11:12 .
 */
public class MenuError {
    public static final ErrorResult Delete_HAVASUBMENU = ErrorResult.create(1100001,"菜单有子菜单，不能删除");
    public static final ErrorResult Delete_FAIL = ErrorResult.create(1100002,"删除失败");
    public static final ErrorResult EDIT_PARENTMOVE = ErrorResult.create(1100003,"顶级菜单不能修改为其他菜单的子菜单");
    public static final ErrorResult EDIT_PARENTSREF = ErrorResult.create(1100004,"顶级菜单没有链接");
    public static final ErrorResult EDIT_PARENT = ErrorResult.create(1100005,"顶级菜单没有链接且不能修改为其他菜单的子菜单");
    public static final ErrorResult ADD_PARENTSREF = ErrorResult.create(1100006,"顶级菜单没有链接");
}
