package com.huatu.ztk.scm.common;

public class Tag {
  private String tagName;
  private String remark;
  private String module;
  private String branch;
  private String createTime;
  private String createBy;

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getModule() {
    return module;
  }

  public void setModule(String module) {
    this.module = module;
  }

  public String getCreateTime() {
    return createTime;
  }

  public void setCreateTime(String createTime) {
    this.createTime = createTime;
  }

  public String getCreateBy() {
    return createBy;
  }

    @Override
    public String toString() {
        return "Tag{" +
                "tagName='" + tagName + '\'' +
                ", remark='" + remark + '\'' +
                ", module='" + module + '\'' +
                ", branch='" + branch + '\'' +
                ", createTime='" + createTime + '\'' +
                ", createBy='" + createBy + '\'' +
                '}';
    }

    public void setCreateBy(String createBy) {
    this.createBy = createBy;
  }

  public String getTagName() {
    return tagName;
  }

  public void setTagName(String tagName) {
    this.tagName = tagName;
  }

  public String getRemark() {
    return remark;
  }

  public void setRemark(String remark) {
    this.remark = remark;
  }
}
