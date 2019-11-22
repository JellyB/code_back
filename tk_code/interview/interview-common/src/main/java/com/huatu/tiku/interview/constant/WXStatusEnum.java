package com.huatu.tiku.interview.constant;


import lombok.AllArgsConstructor;

/**
 * @Author jbzm
 * @Date Create on 2018/1/16 22:22
 */
public class WXStatusEnum {

    @AllArgsConstructor
    public enum BizStatus {
        ONLINE(1, "上线"), OFFLINE(0, "下线");


        private int bizSatus;
        private String description;

        public Integer getBizSatus() {
            return bizSatus;
        }

        public void setBizSatus(int bizSatus) {
            this.bizSatus = bizSatus;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }


    }

    @AllArgsConstructor
    public enum Status {
        NORMAL(1, "正常"), DELETE(-1, "删除");
        private int status;
        private String description;

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

}
