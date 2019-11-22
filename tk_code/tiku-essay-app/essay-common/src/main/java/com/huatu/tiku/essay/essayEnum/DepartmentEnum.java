package com.huatu.tiku.essay.essayEnum;

import com.huatu.tiku.essay.util.enu.EnumUtils;
import com.huatu.tiku.essay.util.enu.IEnum;

/**
 * Created by duanxiangchao on 2019/7/9
 */
public enum DepartmentEnum implements IEnum<Integer> {

    //TODO dxc
    RENDA(1, "人大", true),
    DANGQUN(2, "党群", false),
    ZHENGXIE(3, "政协", false),
    ZONGHE(4, "综合", false);

    private Integer value;
    private String title;
    private Boolean selected;

    private DepartmentEnum(Integer value, String title, Boolean selected) {
        this.value = value;
        this.title = title;
        this.selected = selected;
    }

    public static DepartmentEnum create(Integer value) {
        if(null == value){
            return RENDA;
        }
        for (DepartmentEnum departmentEnum : values()) {
            if(departmentEnum.getValue().equals(value)){
                return departmentEnum;
            }
        }
        return RENDA;
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

}
