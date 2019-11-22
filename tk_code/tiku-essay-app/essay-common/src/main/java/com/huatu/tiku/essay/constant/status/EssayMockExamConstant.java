package com.huatu.tiku.essay.constant.status;

/**
 * Created by x6 on 2017/12/28.
 */
public class EssayMockExamConstant {
    public  enum EssayMockExamBizStatusEnum {

        /*  bizStatus   0初始化  1已关联 2已上线  3已结束(距离结束时间不超过15分钟)  4已完成(距离结束时间已超过15分钟)*/
        CONNECTED(1, "已关联"), ONLINE(2, "已上线"), FINISHED(3, "已结束"), COMPLETED(4, "已完成"), INIT(0, "未关联");

        private EssayMockExamBizStatusEnum(int bizStatus, String description) {
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




    public   enum EssayMockExamStatusEnum {
        NORMAL(1, "正常"), DELETED(-1, "已删除");

        private EssayMockExamStatusEnum(int status, String description) {
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
