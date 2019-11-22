package com.huatu.tiku.constants.teacher;

import lombok.Data;

@Data
public class EssayConstant {
    public enum EssayPracticeType {
        /* 1.已关联   2.已上线   3.已结束*/
        CONNECTED(1, "已关联"), ONLINE(2, "已上线"), FINISHED(3, "已结束"), UPDATE(4, "修改"), OFFLINE(5, "下线");

        private int type;
        private String description;

        EssayPracticeType(int type, String description) {
            this.type = type;
            this.description = description;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    }

