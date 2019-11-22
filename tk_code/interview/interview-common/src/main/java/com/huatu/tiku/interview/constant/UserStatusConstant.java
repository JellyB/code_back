package com.huatu.tiku.interview.constant;

import lombok.AllArgsConstructor;

/**
 * @Author jbzm
 * @Date Create on 2018/1/17 11:57
 */
@AllArgsConstructor
public enum UserStatusConstant {

    NO_INFO(1),
    NO_REPORT(2),
    EXIST_REPORT(3);
    private Integer status;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }


    @AllArgsConstructor
    public enum BizStatus {
        COMPLETED(2, "信息已完善"),BIND(1, "已绑定手机号"), UN_BIND(0, "未绑定手机号");


        private int bizSatus;
        private String description;

        public Integer getBizSatus() {
            return bizSatus;
        }

        public void setBizStatus(int bizSatus) {
            this.bizSatus = bizSatus;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }


    }

}
