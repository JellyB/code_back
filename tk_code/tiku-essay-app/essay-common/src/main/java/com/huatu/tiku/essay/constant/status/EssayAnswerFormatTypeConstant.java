package com.huatu.tiku.essay.constant.status;

/**
 * Created by huangqp on 2017\12\7 0007.
 */
public class EssayAnswerFormatTypeConstant {
    public  enum  EssayAnswerFormatTypeEnum {
        //格式类型，1代表只有标题；2代表有标题、称呼；3代表有标题、落款；4代表有标题、称呼和落款；5没有任何格式
        ONLYTITLE(1,"标题"), TITLEANDAPPELLATION(2, "标题、称呼"),TITLEANDINSCRIBE(3, "标题、落款"),TITLEANDAPPELLATIONANDINSCRIBE(4,"标题、称呼和落款"),NULL(5,"没有任何格式");


        private EssayAnswerFormatTypeEnum(int typeId, String description) {
            this.typeId = typeId;
            this.description = description;
        }



        private int typeId;
        private String description;
        public int getTypeId() {
            return typeId;
        }

        public void setTypeId(int typeId) {
            this.typeId = typeId;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

}
