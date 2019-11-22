package com.huatu.ztk.scm.dto;

/**
 * 用户基本信息 对应表scm_user
 *
 * @author shaojieyue
 * @date 2013-07-11 14:30:26
 */
public class User {
    private int id;
    private String passport;
    private String createBy;
    private int status;
    private String userName;
    private String pwd;
	private String passportInc;
    private int role;

	public String getPassportInc() {
		return passportInc;
	}

	public void setPassportInc(String passportInc) {
		this.passportInc = passportInc;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getPassport() {
		return passport;
	}
	public void setPassport(String passport) {
		this.passport = passport;
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
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	@Override
	public String toString() {
		return "User{" +
				"id=" + id +
				", passport='" + passport + '\'' +
				", createBy='" + createBy + '\'' +
				", status=" + status +
				", userName='" + userName + '\'' +
				", pwd='" + pwd + '\'' +
				", passportInc='" + passportInc + '\'' +
				", role=" + role +
				'}';
	}
}
