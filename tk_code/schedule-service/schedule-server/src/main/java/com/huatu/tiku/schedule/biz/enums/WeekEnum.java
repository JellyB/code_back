package com.huatu.tiku.schedule.biz.enums;

/**
 * Created by duanxiangchao on 2018/5/14
 */
public enum  WeekEnum {

    MONDAY(1, "一", "星期一"),
    TUESDAY(2, "二", "星期二"),
    WEDNESDAY(3, "三", "星期三"),
    THURSDAY(4, "四", "星期四"),
    FRIDAY(5, "五", "星期五"),
    SATURDAY(6, "六", "星期六"),
    SUNDAY(7, "日", "星期日");



    private Integer key;

    private String value;

    private String description;

    WeekEnum(Integer key, String value, String description){
        this.key = key;
        this.value = value;
        this.description = description;
    }

    public Integer getKey() {
        return key;
    }

    public void setKey(Integer key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static WeekEnum create(String value){
        for(WeekEnum weekEnum: WeekEnum.values()){
            if(weekEnum.getValue().equals(value)){
                return weekEnum;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"key\":")
                .append(key);
        sb.append(",\"value\":\"")
                .append(value).append('\"');
        sb.append(",\"description\":\"")
                .append(description).append('\"');
        sb.append('}');
        return sb.toString();
    }
}
