package com.huatu.tiku.essay.constant.status;

/**
 * Created by huangqp on 2017\11\23 0023.
 */
public class EssayPaperBaseConstant {


    public  enum EssayPaperBizStatusEnum {
        //1上线中 0下线中，未上线
        ONLINE(1, "上线中"), OFFLINE(0, "初始状态(未上线,已下线)");

        private EssayPaperBizStatusEnum(int bizStatus, String description) {
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




    public   enum EssayPaperStatusEnum {
        //-1 删除状态  1未审核  2审核中  3审核未通过  4 审核通过
        UN_CHECK(1, "未审核"),CHECKING(2, "审核中"),CHECK_FAILURE(3, "审核未通过"),CHECK_PASS(4, "审核通过"), DELETED(-1, "已删除");

        private EssayPaperStatusEnum(int status, String description) {
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
