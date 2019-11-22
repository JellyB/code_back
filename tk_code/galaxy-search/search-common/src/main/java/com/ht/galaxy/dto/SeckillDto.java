package com.ht.galaxy.dto;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * SeckillDto
 * 
 * @author Geek-S
 *
 */
@Getter
@Setter
@ToString
public class SeckillDto {

	private Integer code;

	private List<Data> data;

	/**
	 * Data
	 * 
	 * @author Geek-S
	 *
	 */
	@Getter
	@Setter
	@ToString
	public static class Data {

		private Integer duartion;

		private Integer number;

		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
		private Date time;
	}
}
