package com.huatu.tiku.schedule.base.config;

import java.util.Collection;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import com.huatu.tiku.schedule.biz.domain.Role;
import com.huatu.tiku.schedule.biz.enums.ExamType;
import com.huatu.tiku.schedule.biz.enums.TeacherType;

import lombok.Getter;
import lombok.Setter;

/**
 * 自定义用户，用于扩展字段
 * 
 * @author Geek-S
 *
 */
@Getter
@Setter
public class CustomUser extends User {

	private static final long serialVersionUID = -8942855898320548559L;

	public CustomUser(Long id, String username, String password, boolean enabled,
			Collection<? extends GrantedAuthority> authorities) {
		super(username, password, enabled, true, true, true, authorities);
		this.id = id;
	}

	/**
	 * 用户ID
	 */
	private Long id;

	/**
	 * 用户电话
	 */
	private String phone;

	/**
	 * 用户名
	 */
	private String name;

	/**
	 * 考试类型
	 */
	private ExamType examType;

	/**
	 * 科目id
	 */
	private Long subjectId;

	/**
	 * 类型 0 组员 1 组长
	 */
	private Boolean leaderFlag;

	/**
	 * 角色
	 */
	private Set<Role> roles;

	/**
	 * 教师类型
	 */
	private TeacherType teacherType;

	/**
	 * 数据权限
	 */
	private Set<ExamType> dataPermissions;
}
