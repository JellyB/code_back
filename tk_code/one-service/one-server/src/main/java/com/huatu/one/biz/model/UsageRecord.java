package com.huatu.one.biz.model;

import com.huatu.one.base.model.BaseModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 使用记录
 *
 * @author geek-s
 * @date 2019-08-26
 */
@Getter
@Setter
@ToString
public class UsageRecord extends BaseModel {

    /**
     * 微信ID
     */
    private String openid;

    /**
     * 使用功能
     */
    private Integer menuId;
}
