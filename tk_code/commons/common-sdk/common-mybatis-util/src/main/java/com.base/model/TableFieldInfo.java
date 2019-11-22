package com.base.model;

import com.base.annotations.IsNotUnderlineToCamel;
import com.base.utils.StringUtil;

/**
 * Created by junli on 2017/9/19.
 */
public class TableFieldInfo {

    /**
     * 字段名称
     */
    private String column;

    /**
     * 属性名称
     */
    private String property;

    public TableFieldInfo(String property, IsNotUnderlineToCamel isNotUnderlineToCamel) {
        this.property = property;
        boolean isUnderlineToCamelVal = StringUtil.checkObjectNotNull(isNotUnderlineToCamel) ? isNotUnderlineToCamel.value() : false;
        this.column = isUnderlineToCamelVal ? this.property : StringUtil.camelToUnderline(this.property);
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

}
