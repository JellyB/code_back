package com.huatu.tiku.schedule.biz.domain;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import com.huatu.tiku.schedule.base.domain.BaseDomain;

import lombok.Getter;
import lombok.Setter;

/**
 * 菜单
 * 
 * @author Geek-S
 *
 */
@Entity
@Getter
@Setter
public class Menu extends BaseDomain {

	private static final long serialVersionUID = 5730966574014090038L;

	/**
	 * 名称
	 */
	private String name;

	/**
	 * 路由
	 */
	private String route;

	/**
	 * 父级菜单
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parentId", insertable = false, updatable = false)
	private Menu parentMenu;

	/**
	 * 父级菜单ID
	 */
	private Long parentId;

	/**
	 * 角色
	 */
	@ManyToMany(mappedBy = "menus")
	private Set<Role> roles;

}
