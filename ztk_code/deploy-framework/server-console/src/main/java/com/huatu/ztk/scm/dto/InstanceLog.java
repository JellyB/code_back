/**
 * Sohu.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.huatu.ztk.scm.dto;

import java.util.Date;

/**
 * 服务日志
 * @author wenpingliu
 * @version v 0.1 15/6/16 11:05 wenpingliu Exp $$
 */
public class InstanceLog {

    public enum OPER_TYPE_ENUM{
        SERVEROP(1,"服务部署"),
        UPDATE_INFO(2,"修改实例"),
        INSTANCEOP(3,"删除增加IP");

        int operType;
        String opertypeDesc;

        private OPER_TYPE_ENUM(int operType,String typeDesc){
           this.operType = operType;
           this.opertypeDesc = typeDesc;
        }

        public int getOperType() {
            return operType;
        }

        public String getOpertypeDesc() {
            return opertypeDesc;
        }

    }


    private int    id;
    private int projectId;
    private String instanceId;
    private int    userId;
    private int    operType;
    private String logMessage;
    private String createBy;
    private Date   createDate;

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getOperType() {
        return operType;
    }

    public void setOperType(int operType) {
        this.operType = operType;
    }

    public String getLogMessage() {
        return logMessage;
    }

    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }
}
