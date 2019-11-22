package com.huatu.tiku.essay.constant.status;

/**
 * Created by huangqp on 2017\11\23 0023.
 */
public class EssayAnswerConstant {
	/**
	 * 状态4 和5 只有人工批改才有
	 * @author zhangchong
	 *
	 */
    public  enum EssayAnswerBizStatusEnum {

        UNFINISHED(1, "未完成"), COMMIT(2, "已交卷"), CORRECT(3, "已批改"), INIT(0, "空白"),CORRECTING(4,"批改中"),CORRECT_RETURN(5,"被退回");

        private EssayAnswerBizStatusEnum(int bizStatus, String description) {
            this.bizStatus = bizStatus;
            this.description = description;
        }

        public static EssayAnswerBizStatusEnum create(int bizStatus){
            for (EssayAnswerBizStatusEnum essayAnswerBizStatusEnum : values()) {
                if(essayAnswerBizStatusEnum.bizStatus == bizStatus){
                    return essayAnswerBizStatusEnum;
                }
            }
            return null;
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




    public   enum EssayAnswerStatusEnum {
        NORMAL(1, "正常"), DELETED(-1, "已删除");

        private EssayAnswerStatusEnum(int status, String description) {
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
