package com.huatu.tiku.essay.constant.status;

/**
 * Created by x6 on 2017/11/26.
 */
public class EssayQuestionTypeConstant {

    public  enum EssayQuestionTypeBizStatusEnum {

        USEFUL(1, "可用状态"), DELETED(2, "删除状态"), INIT(0, "初始状态");

        private EssayQuestionTypeBizStatusEnum(int bizStatus, String description) {
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




    public   enum EssayQuestionTypeStatusEnum {
        NORMAL(1, "正常"), DELETED(-1, "已删除");

        private EssayQuestionTypeStatusEnum(int status, String description) {
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
