package com.huatu.ztk.scm.dto;

import java.util.Date;
/**
 * 表smc_project 对应的bean
 * @author shaojieyue
 * @date 2013-07-12 17:09:16
 */
public class Project {

    private int    id;
    private String gitUrl;
    private String projectName;
    private Date   createDate;
    private String createBy;
    private String remark;
    private String currentTag;
    private String newestTag;
    private String testBranch;
    private String developBranch;

    public String getDevelopBranch() {
        return developBranch;
    }

    public void setDevelopBranch(String developBranch) {
        this.developBranch = developBranch;
    }

    public String getTestBranch() {
        return testBranch;
    }

    public void setTestBranch(String testBranch) {
        this.testBranch = testBranch;
    }

    public String getNewestTag() {
        return newestTag;
    }

    public void setNewestTag(String newestTag) {
        this.newestTag = newestTag;
    }

    private int permissions;//权限

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getGitUrl() {
        return gitUrl;
	}
	public void setGitUrl(String gitUrl) {
		this.gitUrl = gitUrl;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public String getCurrentTag() {
		return currentTag;
	}
	public void setCurrentTag(String currentTag) {
		this.currentTag = currentTag;
	}
	public int getPermissions() {
		return permissions;
	}
	public void setPermissions(int permissions) {
		this.permissions = permissions;
	}
	@Override
	public String toString() {
		return "Project [id=" + id + ", gitUrl=" + gitUrl + ", projectName="
				+ projectName + ", createDate=" + createDate + ", createBy="
				+ createBy + ", remark=" + remark + ", currentTag="
				+ currentTag + ", permissions=" + permissions + "]";
	}
	
}
