package com.base.model;

import java.util.List;

/**
 * Created by junli on 2017/9/19.
 */
public class TableInfo {

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 字段集合
     */
    private List<TableFieldInfo> fieldInfoList;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<TableFieldInfo> getFieldInfoList() {
        return fieldInfoList;
    }

    public void setFieldInfoList(List<TableFieldInfo> fieldInfoList) {
        this.fieldInfoList = fieldInfoList;
    }
}
