package com.huatu.tiku.essay.essayEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;


public enum EssayAnswerCardEnum {
    ;

    @AllArgsConstructor
    @Getter
    public enum TypeEnum {
        PAPER(1, "套卷"),
        QUESTION(0, "单题"),;
        private int type;
        private String name;
    }

    @AllArgsConstructor
    @Getter
    public enum ModeTypeEnum{
        NORMAL(1,"普通模式"),
        COURSE(2,"课后模式");
        private int type;
        private String name;

        public static ModeTypeEnum create(Integer answerCardType) {
            if(null == answerCardType){
                answerCardType = 1;
            }
            for (ModeTypeEnum value : ModeTypeEnum.values()) {
                if(answerCardType.intValue() == value.getType()){
                    return value;
                }
            }
            return NORMAL;
        }
    }
}
