package com.huatu.one.biz.model;

import com.huatu.one.base.model.BaseModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 用户
 *
 * @author geek-s
 * @date 2019-08-26
 */
@Getter
@Setter
@ToString
public class User extends BaseModel {

    /**
     * 微信ID
     */
    private String openid;

    /**
     * 姓名
     */
    private String username;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 老师ID
     */
    private Long teacherId;

    /**
     * 老师类型
     */
    private String teacherType;

    /**
     * 1 审核中 2 正常 3 禁用
     */
    private Integer status;
}
