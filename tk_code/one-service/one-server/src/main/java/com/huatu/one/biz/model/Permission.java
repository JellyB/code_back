package com.huatu.one.biz.model;

import com.huatu.one.base.model.BaseModel;
import lombok.Data;

/**
 * 权限
 *
 * @author geek-s
 * @date 2019-08-26
 */
@Data
public class Permission extends BaseModel {

    /**
     * 名称
     */
    private String name;

    /**
     * 资源（url）
     */
    private String resource;
}
