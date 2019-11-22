package com.huatu.tiku.essay.essayEnum;

import com.huatu.tiku.essay.util.enu.EnumUtils;
import com.huatu.tiku.essay.util.enu.IEnum;

/**
 * Created by duanxiangchao on 2019/7/10
 */
public enum TeacherOrderTypeEnum implements IEnum<Integer> {

    //TODO dxc
    QUESTION(1, "小题", true, "篇", 15,      24,  1),
    PRACTICAL(2, "应用文", false, "篇", 15,    24,  1),
    ARGUMENT(3, "文章写作（议论文）", false, "篇", 15,   24,  1),
    SET_QUESTION(4, "套题", false, "套", 45,  48,  2);


    private Integer value;
    private String title;
    private Boolean selected;
    private String unit;
    private Integer salary;
    //正常完成时间小时
    private Integer completeTime;
    //顺延时间单位天
    private Integer delayTime;

    /**
     * 1,2,3,4,5 转换成 TeacherOrderTypeEnum
     * @param questionType
     * @return
     */
    public static TeacherOrderTypeEnum convert(int questionType){
        switch (questionType){
            case 5:
                return ARGUMENT;
            case 4:
                return PRACTICAL;
            default:
                return QUESTION;
        }
    }
    /**
     * 交卷成功提示
     * @param orderTypeEnum
     * @return
     */
    public static String submitContent(TeacherOrderTypeEnum orderTypeEnum, Integer delayStatus){
        int hours = orderTypeEnum.getCompleteTime();
        if(null != delayStatus && delayStatus.intValue() == CorrectOrderStatusEnum.DelayStatusEnum.YES.getCode()){
            hours +=  orderTypeEnum.getDelayTime() * 24;
        }
        return "交卷成功，预计" + hours + "小时内出批改结果";
    }

    /**
     * 批改记录列表点击在批改中的任务提示
     * @param orderTypeEnum
     * @return
     */
    public static String reportContent(TeacherOrderTypeEnum orderTypeEnum, Integer delayStatus){
        int hours = orderTypeEnum.getCompleteTime();
        if(null != delayStatus && delayStatus.intValue() == CorrectOrderStatusEnum.DelayStatusEnum.YES.getCode()){
            hours +=  orderTypeEnum.getDelayTime() * 24;
        }
        return "正在批改中，交卷" + hours + "小时内出批改结果";
    }

    private TeacherOrderTypeEnum(Integer value, String title, Boolean selected, String unit, Integer salary, Integer completeTime, Integer delayTime) {
        this.value = value;
        this.title = title;
        this.selected = selected;
        this.unit = unit;
        this.salary = salary;
        this.delayTime = delayTime;
        this.completeTime = completeTime;
    }

    public static TeacherOrderTypeEnum create(Integer value) {
        return (TeacherOrderTypeEnum) EnumUtils.getEnum(values(), value);
    }

    public Integer getValue() {
        return this.value;
    }

    public String getTitle() {
        return this.title;
    }

    public Boolean getSelected() {
        return this.selected;
    }

    public String getUnit() {
        return this.unit;
    }

    public Integer getSalary() {
        return this.salary;
    }

    public Integer getDelayTime() {
        return this.delayTime;
    }
    
    public Integer getCompleteTime() {
        return this.completeTime;
    }

}
