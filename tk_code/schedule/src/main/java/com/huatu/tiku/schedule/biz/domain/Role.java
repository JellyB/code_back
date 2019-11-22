package com.huatu.tiku.schedule.biz.domain;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import com.huatu.tiku.schedule.base.domain.BaseDomain;

import lombok.Getter;
import lombok.Setter;

/**
 * 角色
 * 
 * @author Geek-S
 *
 */
@Entity
@Getter
@Setter
public class Role extends BaseDomain {

	private static final long serialVersionUID = 5730966574014090038L;

	/**
	 * 名称
	 */
	private String name;

	/**
	 * 教师
	 */
	@ManyToMany(mappedBy = "roles")
	private Set<Teacher> teachers;

	/**
	 * 权限
	 */
	@ManyToMany
	@JoinTable(joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "authority_id"))
	private Set<Authority> authorities;

	/**
	 * 菜单
	 */
	@ManyToMany
	@JoinTable(joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "menu_id"))
	private Set<Menu> menus;

}
