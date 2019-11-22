package com.huatu.ztk.scm.dto;
/**
 * 表 scm_menu对应的dto
 * @author shaojieyue
 * @date 2013-08-16 14:18:15
 */
public class Menu {
	private int id;
	private String menuName;
	private String menuUrl;
	private int level;
	private String remark;
	private String createBy;
	private String userCode;
	public String getMenuName() {
		return menuName;
	}
	public void setMenuName(String menuName) {
		this.menuName = menuName;
	}
	public String getMenuUrl() {
		return menuUrl;
	}
	public void setMenuUrl(String menuUrl) {
		this.menuUrl = menuUrl;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	@Override
	public String toString() {
		return "Menu [id=" + id + ", menuName=" + menuName + ", menuUrl="
				+ menuUrl + ", level=" + level + ", remark=" + remark
				+ ", createBy=" + createBy + ", userCode=" + userCode + "]";
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCreateBy() {
		return createBy;
	}
	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}
	public String getUserCode() {
		return userCode;
	}
	public void setUserCode(String userCode) {
		this.userCode = userCode;
	}
}
