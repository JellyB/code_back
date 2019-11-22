package com.huatu.tiku.schedule.biz.dto;

import java.io.Serializable;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 接收IDs
 * 
 * @author Geek-S
 *
 */
@Getter
@Setter
@ToString
public class IdsDto implements Serializable {

	private static final long serialVersionUID = 5730966574014090038L;

	/**
	 * IDs
	 */
	@NotEmpty(message = "ID不能为空")
	private List<Long> ids;

}
