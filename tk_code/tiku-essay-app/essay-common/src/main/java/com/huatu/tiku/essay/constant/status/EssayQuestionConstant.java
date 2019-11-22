package com.huatu.tiku.essay.constant.status;

/**
 * Created by huangqp on 2017\11\22 0022.
 */
public class EssayQuestionConstant {

    public  enum EssayQuestionBizStatusEnum {

        ONLINE(1, "上线中"),  OFFLINE(0, "未上线（初始状态）"),  LOSE(2, "缺失");

        private EssayQuestionBizStatusEnum(int bizStatus, String description) {
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




    public   enum EssayQuestionStatusEnum {
        NORMAL(1, "正常"), DELETED(-1, "已删除");

        private EssayQuestionStatusEnum(int status, String description) {
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
