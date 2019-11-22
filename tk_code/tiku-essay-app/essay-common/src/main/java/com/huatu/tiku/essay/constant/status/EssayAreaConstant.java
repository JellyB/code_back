package com.huatu.tiku.essay.constant.status;

/**
 * Created by x6 on 2017/12/3.
 */
public class EssayAreaConstant {


    public  enum EssayAreaBizStatusEnum {

        ONLINE(1, "上线"),  INIT(0, "初始化（下线）");

        private EssayAreaBizStatusEnum(int bizStatus, String description) {
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




    public   enum EssayAreaStatusEnum {
        NORMAL(1, "正常"), DELETED(-1, "已删除");

        private EssayAreaStatusEnum(int status, String description) {
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
