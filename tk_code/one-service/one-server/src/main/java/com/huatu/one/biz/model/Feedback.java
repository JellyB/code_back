package com.huatu.one.biz.model;

import com.huatu.one.base.model.BaseModel;
import lombok.Data;

/**
 * 意见反馈
 */
@Data
public class Feedback extends BaseModel {
    /**
     * 用户openid
     */
    private String openid;

    /**
     * 反馈意见
     */
    private String content;

    /**
     * 可用状态
     */
    private Integer status;
}
