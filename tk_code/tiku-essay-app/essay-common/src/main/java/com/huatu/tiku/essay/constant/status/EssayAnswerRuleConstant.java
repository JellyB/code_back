package com.huatu.tiku.essay.constant.status;

/**
 * Created by huangqp on 2017\12\7 0007.
 */
public class EssayAnswerRuleConstant {
    public  enum EssayAnswerRuleBizStatusEnum {

        ONLINE(0,"上线"), INIT(1, "初始化");

        private EssayAnswerRuleBizStatusEnum(int bizStatus, String description) {
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




    public   enum EssayAnswerRuleStatusEnum {
        NORMAL(1, "正常"), DELETED(-1, "已删除");

        private EssayAnswerRuleStatusEnum(int status, String description) {
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
