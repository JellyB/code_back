package com.huatu.tiku.schedule.biz.vo;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * 左侧菜单
 * 
 * @author Geek-S
 *
 */
@Getter
@Setter
public class LeftMenuVo implements Serializable {

	private static final long serialVersionUID = -7869509529945933530L;

	// 一级名称
	private String title;

	// 二级菜单
	private List<SubMenu> list;

	@Getter
	@Setter
	public static class SubMenu implements Serializable {

		private static final long serialVersionUID = 604728650753484083L;

		// 菜单名称
		private String bt;

		// 路由
		private String link;
	}
}
