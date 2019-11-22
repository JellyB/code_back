package com.huatu.ztk.paper.enums;

/**
 * 专项训练抽题相关枚举
 */
public enum CustomizeEnum {
    ;
    public enum ModeEnum{
        Write(1,"做题模式"),
        Look(2,"背题模式"),
        Default(-1,"未知模式"),
        ;
        private int key;
        private String name;

        ModeEnum(int key, String name) {
            this.key = key;
            this.name = name;
        }

        public int getKey() {
            return key;
        }

        public void setKey(int key) {
            this.key = key;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public static ModeEnum create(int key){
            for (ModeEnum modeEnum : ModeEnum.values()) {
                if(modeEnum.getKey() == key){
                    return modeEnum;
                }
            }
            return Default;
        }
    }
}
