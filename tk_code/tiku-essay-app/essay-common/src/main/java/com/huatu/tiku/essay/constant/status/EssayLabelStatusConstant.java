package com.huatu.tiku.essay.constant.status;

/**
 * 议论文标注相关状态
 */
public class EssayLabelStatusConstant {


    public  enum EssayLabelBizStatusEnum {
        /**
         *  INIT只保存了详细批注
         *  online 完成
         */
        INIT(0, "未上线（初始状态）"),
        ONLINE(1, "已上线(完成批注)"),
        FINISH(2, "完成视频转码"),
        ;

        private EssayLabelBizStatusEnum(int bizStatus, String description) {
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




    public   enum EssayLabelStatusEnum {
        NORMAL(1, "正常"), DELETED(-1, "已删除");

        private EssayLabelStatusEnum(int status, String description) {
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
