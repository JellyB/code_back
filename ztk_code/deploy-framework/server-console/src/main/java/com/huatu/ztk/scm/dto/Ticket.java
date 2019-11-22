/**
 * Sohu.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.huatu.ztk.scm.dto;

/**
 *
 * @author wenpingliu
 * @version v 0.1 15/9/16 22:26 wenpingliu Exp $$
 */
public class Ticket {
    int id;
    String serverName;
    String projectName;
    String module;
    String createBy;
    int status;
    int type;
    String tester;
    String deployer;
    String releaseLog;
    String branch;

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getReleaseLog() {
        return releaseLog;
    }

    public void setReleaseLog(String releaseLog) {
        this.releaseLog = releaseLog;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTester() {
        return tester;
    }

    public void setTester(String tester) {
        this.tester = tester;
    }

    public String getDeployer() {
        return deployer;
    }

    public void setDeployer(String deployer) {
        this.deployer = deployer;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
