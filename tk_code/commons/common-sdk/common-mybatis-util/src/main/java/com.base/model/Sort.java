package com.base.model;

import com.base.utils.StringUtil;

/**
 * 排序字段
 * Created by junli on 2017/9/25.
 */
public class Sort {
    private String field;

    /**
     * true 表示降序
     */
    private boolean flag;

    public Sort(String field, boolean flag) {
        this.field = StringUtil.camelToUnderline(field);
        this.flag = flag;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }
}
