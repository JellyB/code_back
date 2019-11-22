package com.huatu.tiku.schedule.biz.domain;

import javax.persistence.*;

import com.huatu.tiku.schedule.base.domain.BaseDomain;
import com.huatu.tiku.schedule.biz.enums.ExamType;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 科目
 * 
 * @author Geek-S
 *
 */
@Entity
@Getter
@Setter
public class Subject extends BaseDomain {

	private static final long serialVersionUID = 5730966574014090038L;

	/**
	 * 考试类型
	 */
	private ExamType examType;

	/**
	 * 名称
	 */
	private String name;

	/**
	 * 是否显示
	 */
	private Boolean showFlag;

	/**
	 * 排序
	 */
	private Integer sort;

	public Subject(){
		this.sort=0;
	}
}
