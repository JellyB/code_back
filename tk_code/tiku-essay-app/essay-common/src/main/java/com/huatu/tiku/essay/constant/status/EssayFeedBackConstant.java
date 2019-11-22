package com.huatu.tiku.essay.constant.status;

/**
 * 意见反馈状态
 */
public class EssayFeedBackConstant {

    //     * @param processed(0全部 1已回复2未回复)
    //     * @param isSolve(0全部 1已处理 2未处理 -1已关闭)
    public enum EssayFeedBackQueryStatusEnum {
        REPLYEFD(1, "已回复"), INIT(2, "未回复（初始状态）"), CLOSED(3, "已关闭"), ALL(0, "全部");

        private EssayFeedBackQueryStatusEnum(int status, String description) {
            this.status = status;
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

    public static EssayFeedBackQueryStatusEnum getStatusEnum(Integer status) {
        for (EssayFeedBackQueryStatusEnum statusEnum : EssayFeedBackQueryStatusEnum.values()) {
            if (statusEnum.getStatus() == status) {
                return statusEnum;
            }
        }
        return EssayFeedBackQueryStatusEnum.ALL;
    }


    public enum EssayFeedBackSolveStatusEnum {
        ALL(0, "全部"), PEROCESSED(1, "已处理（废弃）"), UN_PEROCESSED(2, "未处理关闭（废弃）"), CLOSED(-1, "已关闭");

        private EssayFeedBackSolveStatusEnum(int status, String description) {
            this.status = status;
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
