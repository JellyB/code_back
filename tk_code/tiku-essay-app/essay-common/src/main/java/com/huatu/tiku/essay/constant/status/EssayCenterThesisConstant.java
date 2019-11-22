package com.huatu.tiku.essay.constant.status;

/**
 * Created by x6 on 2017/12/3.
 */
public class EssayCenterThesisConstant {


    public  enum EssayCenterThesisBizStatusEnum {

         INIT(0, "初始化"), ADOPTED(1, "已采纳"), UNADOPTED(2, "未采纳");

        private EssayCenterThesisBizStatusEnum(int bizStatus, String description) {
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




    public   enum EssayCenterThesisStatusEnum {
        NORMAL(1, "正常"), DELETED(-1, "已删除");

        private EssayCenterThesisStatusEnum(int status, String description) {
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
