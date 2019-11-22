package com.huatu.tiku.essay.constant.status;

/**
 * Created by x6 on 2018/2/6.
 */
public class EssayCorrectFreeUserConstant {
    public  enum EssayCorrectFreeUserBizStatusEnum {

        ONLINE(1, "上线"), OFFLINE(0, "下线");

        private EssayCorrectFreeUserBizStatusEnum(int bizStatus, String description) {
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




    public   enum EssayCorrectFreeUserStatusEnum {
        NORMAL(1, "正常"), DELETED(-1, "已删除");

        private EssayCorrectFreeUserStatusEnum(int status, String description) {
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
