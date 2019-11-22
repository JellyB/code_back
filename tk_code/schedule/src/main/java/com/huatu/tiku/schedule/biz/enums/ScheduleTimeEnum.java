package com.huatu.tiku.schedule.biz.enums;

/**
 * Created by duanxiangchao on 2018/5/14
 */
public enum ScheduleTimeEnum {

    NOON(1, "上午", 900, 1130),
    AFTERNOON(2, "下午", 1400, 1630),
    NIGHT(3, "晚上", 1900, 2130);

    private Integer key;

    private String value;

    private Integer timeBegin;

    private Integer timeEnd;

    ScheduleTimeEnum(Integer key, String value, Integer timeBegin, Integer timeEnd){
        this.key = key;
        this.value = value;
        this.timeBegin = timeBegin;
        this.timeEnd = timeEnd;
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

    public Integer getTimeBegin() {
        return timeBegin;
    }

    public void setTimeBegin(Integer timeBegin) {
        this.timeBegin = timeBegin;
    }

    public Integer getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(Integer timeEnd) {
        this.timeEnd = timeEnd;
    }

    public static ScheduleTimeEnum getWeekScheduleTimeEnum(WeekEnum weekEnum, Integer count){
        ScheduleTimeEnum scheduleTimeEnum = null;
        switch (weekEnum){
            case MONDAY:
            case TUESDAY:
            case THURSDAY:
            case WEDNESDAY:
            case FRIDAY:
                scheduleTimeEnum = NIGHT;
                break;
            case SATURDAY:
            case SUNDAY:
                if(count == 1){
                    scheduleTimeEnum = NOON;
                    break;
                } else {
                    scheduleTimeEnum = AFTERNOON;
                    break;
                }
        }
        return scheduleTimeEnum;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"key\":")
                .append(key);
        sb.append(",\"value\":\"")
                .append(value).append('\"');
        sb.append(",\"timeBegin\":")
                .append(timeBegin);
        sb.append(",\"timeEnd\":")
                .append(timeEnd);
        sb.append('}');
        return sb.toString();
    }
}
