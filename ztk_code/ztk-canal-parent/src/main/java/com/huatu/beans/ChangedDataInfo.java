package com.huatu.beans;

/**
 * 监控mysql  binlog的变动信息
 * 提取有用的数据,发送到mq中
 * Created by ismyway on 16/5/11.
 */
public class ChangedDataInfo {

    /**
     * 表名
     */
    private String tableName;
    /**
     * 操作类型
     */
    private String option;
    /**
     * 主键
     */
    private int puKey;

    @Override
    public String toString() {
        return "ChangedDataInfo{" +
                "tableName='" + tableName + '\'' +
                ", option='" + option + '\'' +
                ", puKey=" + puKey +
                '}';
    }

    public int getPuKey() {
        return puKey;
    }

    public void setPuKey(int puKey) {
        this.puKey = puKey;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }
}
