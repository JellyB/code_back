package com.huatu.tiku.essay.constant.status;


import lombok.AllArgsConstructor;

public class EssayStatisticsConstant {

    @AllArgsConstructor
    public enum EssayStatisticsStatus {
        ONLINE(1, "上线"), OFFLINE(0, "下线");

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

    @AllArgsConstructor
    public enum EssayStatisticsBizStatus {
        NORMAL(1, "正常"), DELETE(0, "删除");
        private int bizStatus;
        private String description;

        public int getBizStatus() {
            return bizStatus;
        }

        public void setBizStatus(int bizStatus) {
            this.bizStatus = bizStatus;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
