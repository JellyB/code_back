package com.huatu.tiku.essay.constant.status;

/**
 * Created by x6 on 2017/11/29.
 */
public class EssayQuestionAnswerConstant {

    /*public  enum EssayQuestionAnswerBizStatusEnum {
        //0 空白  1未完成  2 已交卷  3已批改
        UNFINISHED(1, "未完成"), COMMIT(2, "已交卷"), CORRECT(3, "已批改"), REJECT(4, "被驳回"), INIT(0, "空白");

        private EssayQuestionAnswerBizStatusEnum(int bizStatus, String description) {
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
    }*/




    public   enum EssayQuestionAnswerStatusEnum {
        RECYCLED(2, "回收状态"),NORMAL(1, "正常"), DELETED(-1, "已删除");

        private EssayQuestionAnswerStatusEnum(int status, String description) {
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
