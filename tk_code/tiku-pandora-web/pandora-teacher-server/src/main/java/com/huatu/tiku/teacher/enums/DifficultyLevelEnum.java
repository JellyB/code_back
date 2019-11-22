package com.huatu.tiku.teacher.enums;


import com.huatu.tiku.enumHelper.EnumUtils;
import com.huatu.tiku.enumHelper.IEnum;

/**
 * Created by duanxiangchao on 2018/5/3
 */
public enum DifficultyLevelEnum implements IEnum<Integer> {

    SO_EASY(2, "容易"),
    EASY(4, "较易"),
    GENERAL(6, "中等"),
    DIFFICULT(8, "较难"),
    SO_DIFFICULT(10, "困难");

    private int value;
    private String title;

    public static DifficultyLevelEnum create(Integer value) {
        DifficultyLevelEnum difficultyLevelEnum = EnumUtils.getEnum(DifficultyLevelEnum.values(), value);
        if (null == difficultyLevelEnum) {
            System.out.println("难度" + value + "未被识别，强制指向一般难度");
            return GENERAL;
        }
        return difficultyLevelEnum;
    }

    public static DifficultyLevelEnum create(String difficult) {
        DifficultyLevelEnum difficultyLevelEnum = EnumUtils.getEnum(DifficultyLevelEnum.values(), difficult);
        if (difficultyLevelEnum == null) {
            System.out.println("难度" + difficult + "未被识别，强制指向一般难度");
            return GENERAL;
        }
        return difficultyLevelEnum;
    }

    DifficultyLevelEnum(int value, String title) {
        this.value = value;
        this.title = title;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return EnumUtils.toJSONString(this);
    }


}
