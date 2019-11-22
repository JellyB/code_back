package com.huatu.tiku.schedule.biz.domain;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;

import com.huatu.tiku.schedule.base.domain.BaseDomain;

import lombok.Getter;
import lombok.Setter;

/**
 * 权限
 * 
 * @author Geek-S
 *
 */
@Entity
@Getter
@Setter
public class Authority extends BaseDomain {

	private static final long serialVersionUID = 5730966574014090038L;

	/**
	 * 名称
	 */
	private String name;

	/**
	 * 权限资源
	 */
	private String resource;

	/**
	 * 说明
	 */
	private String memo;

	/**
	 * 角色
	 */
	@ManyToMany(mappedBy = "authorities")
	private Set<Role> roles;
}
