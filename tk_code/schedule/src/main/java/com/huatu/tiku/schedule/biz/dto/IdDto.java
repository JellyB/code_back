package com.huatu.tiku.schedule.biz.dto;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 接收ID
 * 
 * @author Geek-S
 *
 */
@Getter
@Setter
@ToString
public class IdDto implements Serializable {

	private static final long serialVersionUID = 5730966574014090038L;

	/**
	 * ID
	 */
	@NotNull(message = "ID不能为空")
	private Long id;

}
