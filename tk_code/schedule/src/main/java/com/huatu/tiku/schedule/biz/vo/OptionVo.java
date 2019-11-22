package com.huatu.tiku.schedule.biz.vo;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 前端Checkbox/Dropdown显示
 * 
 * @author Geek-S
 *
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
public class OptionVo implements Serializable {

	private static final long serialVersionUID = -4085191186879887982L;

	/**
	 * 角色ID
	 */
	private Long id;

	/**
	 * 角色名称
	 */
	private String name;

	/**
	 * 是否有该角色
	 */
	private Boolean checked;

}
