package com.huatu.tiku.interview.constant;

import lombok.AllArgsConstructor;

/**
 * @Author jbzm
 * @Date Create on 2018/1/17 11:57
 */
@AllArgsConstructor
public enum UserClassBoundTypeConstant {

    TEMPORARY(0),
    DURABLE(1);
    private Integer status;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
