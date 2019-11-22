package com.huatu.tiku.essay.constant.status;

/**
 * Created by huangqp on 2017\11\22 0022.
 */
public class EssayUserCollectionConstant {

    public  enum EssayUserCollectionBizStatusEnum {

        //暂时没有业务状态。默认上线状态
        ONLINE(1, "上线中"),  OFFLINE(0, "未上线");

        private EssayUserCollectionBizStatusEnum(int bizStatus, String description) {
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




    public   enum EssayUserCollectionStatusEnum {
        NORMAL(1, "正常"), DELETED(-1, "已删除");

        private EssayUserCollectionStatusEnum(int status, String description) {
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
