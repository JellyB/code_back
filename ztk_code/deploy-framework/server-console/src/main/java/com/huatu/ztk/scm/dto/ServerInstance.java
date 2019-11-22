package com.huatu.ztk.scm.dto;

import java.util.Date;
import java.util.List;

/**
 * 表smc_server_instance对应的dto
 * @author shaojieyue
 * @date 2013-07-15 19:02:59
 */
public class ServerInstance{
	private String id;
	private String mainClass;
	private String mainArgs;
	private String jvmArgs;
	private String createBy;
	private String sourcePath;
	private String serverName;
	private String projectName;
	private Date createDate;
	private String remark;
	private int permissions;
    private String serverMode;
	private List<InstanceIp> ips;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getMainClass() {
		return mainClass;
	}
	public void setMainClass(String mainClass) {
		this.mainClass = mainClass;
	}
	public String getMainArgs() {
		return mainArgs;
	}
	public void setMainArgs(String mainArgs) {
		this.mainArgs = mainArgs;
	}
	public String getJvmArgs() {
		return jvmArgs;
	}
	public void setJvmArgs(String jvmArgs) {
		this.jvmArgs = jvmArgs;
	}
	public String getCreateBy() {
		return createBy;
	}
	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}
	public String getSourcePath() {
		return sourcePath;
	}
	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public List<InstanceIp> getIps() {
		return ips;
	}
	public void setIps(List<InstanceIp> ips) {
		this.ips = ips;
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
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}

    public String getServerMode() {
        return serverMode;
    }

    public void setServerMode(String serverMode) {
        this.serverMode = serverMode;
    }

	@Override
	public String toString() {
		return "ServerInstance [id=" + id + ", mainClass=" + mainClass
				+ ", mainArgs=" + mainArgs + ", jvmArgs=" + jvmArgs
				+ ", createBy=" + createBy + ", sourcePath=" + sourcePath
				+ ", serverName=" + serverName + ", projectName=" + projectName
				+ ", createDate=" + createDate + ", remark=" + remark
				+ ", ips=" + ips + ", serverMode=" + serverMode + "]";
	}
	public int getPermissions() {
		return permissions;
	}
	public void setPermissions(int permissions) {
		this.permissions = permissions;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ServerInstance))
            return false;

        ServerInstance that = (ServerInstance) o;

        if (createBy != null ? !createBy.equals(that.createBy) : that.createBy != null)
            return false;
        if (createDate != null ? !createDate.equals(that.createDate) : that.createDate != null)
            return false;
        if (id != null ? !id.equals(that.id) : that.id != null)
            return false;
        if (ips != null ? !ips.equals(that.ips) : that.ips != null)
            return false;
        if (jvmArgs != null ? !jvmArgs.equals(that.jvmArgs) : that.jvmArgs != null)
            return false;
        if (mainArgs != null ? !mainArgs.equals(that.mainArgs) : that.mainArgs != null)
            return false;
        if (mainClass != null ? !mainClass.equals(that.mainClass) : that.mainClass != null)
            return false;
        if (projectName != null ? !projectName.equals(that.projectName) : that.projectName != null)
            return false;
        if (remark != null ? !remark.equals(that.remark) : that.remark != null)
            return false;
        if (serverMode != null ? !serverMode.equals(that.serverMode) : that.serverMode != null)
            return false;
        if (serverName != null ? !serverName.equals(that.serverName) : that.serverName != null)
            return false;
        if (sourcePath != null ? !sourcePath.equals(that.sourcePath) : that.sourcePath != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (mainClass != null ? mainClass.hashCode() : 0);
        result = 31 * result + (mainArgs != null ? mainArgs.hashCode() : 0);
        result = 31 * result + (jvmArgs != null ? jvmArgs.hashCode() : 0);
        result = 31 * result + (createBy != null ? createBy.hashCode() : 0);
        result = 31 * result + (sourcePath != null ? sourcePath.hashCode() : 0);
        result = 31 * result + (serverName != null ? serverName.hashCode() : 0);
        result = 31 * result + (projectName != null ? projectName.hashCode() : 0);
        result = 31 * result + (createDate != null ? createDate.hashCode() : 0);
        result = 31 * result + (remark != null ? remark.hashCode() : 0);
        result = 31 * result + (serverMode != null ? serverMode.hashCode() : 0);
        result = 31 * result + (ips != null ? ips.hashCode() : 0);
        return result;
    }
}
