package com.huatu.ztk.scm.dto;
/**
 * scm_instance_ip 对应的bean
 * @author shaojieyue
 * @date 2013-07-18 15:57:08
 */
public class InstanceIp {
	private int id;
	private String ip;
	private String instanceId;
	private String createBy;
	private String createDate;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getInstanceId() {
		return instanceId;
	}
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	public String getCreateBy() {
		return createBy;
	}
	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}
	public String getCreateDate() {
		return createDate;
	}
	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}
}
