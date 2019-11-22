package com.huatu.tiku.position.biz.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

/**机构层级
 * @author wangjian
 **/
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum DepartmentType {

    ZY("中央"),SJ("省（副省）级"),DJ("市（地）级"),XJ("县（区）级及以下");

    /**
     * 值
     */
    private String value;

    /**
     * 显示字体
     */
    private String text;

    private DepartmentType(String text) {
        this.value=name();
        this.text = text;
    }

    public static DepartmentType findByString(String string){
        for (DepartmentType departmentType : DepartmentType.values()) {
            if(departmentType.text.equals(string)){
                return departmentType;
            }
        }
        return null;
    }
}
