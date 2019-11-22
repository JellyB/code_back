package com.huatu.tiku.essay.constant.status;

/**
 * Created by x6 on 2017/11/22.
 */
public class UserCorrectGoodsConstant {

    public  enum UserCorrectGoodsBizStatusEnum {

        NORMAL(1, "正常状态"), INIT(0, "初始状态");

        private UserCorrectGoodsBizStatusEnum(int bizStatus, String description) {
            this.bizStatus = bizStatus;
            this.description = description;
        }

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

    public   enum UserCorrectGoodsStatusEnum {
        NORMAL(1, "正常"), DELETED(-1, "已删除");

        private UserCorrectGoodsStatusEnum(int status, String description) {
            this.status  = status;
            this.description = description;
        }

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
