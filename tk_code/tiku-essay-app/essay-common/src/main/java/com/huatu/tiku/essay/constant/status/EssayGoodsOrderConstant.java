package com.huatu.tiku.essay.constant.status;

/**
 * Created by x6 on 2017/11/22.
 */
public class EssayGoodsOrderConstant {

    public  enum EssayGoodsOrderBizStatusEnum {

        PAYED(1, "支付成功",1),
        CANCEL(2, "取消支付",2),
        PAYEXCEPTION(3, "支付异常",-1),
        TIMEOUT(4, "超时取消",2),
        PRE_BACK(5, "退款中",5),
        BACKED(6, "已退款",6),
        BACKED_REJECT(7, "退款驳回",7),
        INIT(0, "等待付款",0),
        ;

        private EssayGoodsOrderBizStatusEnum(int bizStatus, String description,int status) {
            this.bizStatus = bizStatus;
            this.description = description;
            this.status = status;
        }

        private int bizStatus;
        private String description;
        private int status;

        public static EssayGoodsOrderBizStatusEnum create(int bizStatus) {
            for (EssayGoodsOrderBizStatusEnum value : EssayGoodsOrderBizStatusEnum.values()) {
                if(value.getBizStatus() == bizStatus){
                    return value;
                }
            }
            return null;
        }

        /**
         * 业务查询用户的状态名称
         * @param bizStatus
         * @return
         */
        public static String getName(int bizStatus) {
            EssayGoodsOrderBizStatusEnum bizStatusEnum = create(bizStatus);
            if(null == bizStatusEnum || bizStatusEnum.getStatus() == -1){
                return "支付异常";
            }
            if(bizStatusEnum.getStatus() == bizStatusEnum.getBizStatus()){
                return bizStatusEnum.getDescription();
            }
            return create(bizStatusEnum.getStatus()).getDescription();
        }
        /**
         * 业务查询用户的业务状态ID
         * @param bizStatus
         * @return
         */
        public static Integer getBizId(int bizStatus) {
            EssayGoodsOrderBizStatusEnum bizStatusEnum = create(bizStatus);
            if(null == bizStatusEnum){
                return -1;
            }
            return bizStatusEnum.getStatus();
        }

        /**
         * 校验状态是否可用
         * @param bizStatus
         * @return
         */
        public static boolean available(int bizStatus) {
            return BACKED_REJECT.getStatus() == bizStatus || PAYED.getStatus() == bizStatus;
        }

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

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
    }

    public   enum EssayGoodsOrderStatusEnum {
        NORMAL(1, "正常"), DELETED(-1, "已删除");

        private EssayGoodsOrderStatusEnum(int status, String description) {
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
