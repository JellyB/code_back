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

	@OneToMany(mappedBy = "subject",fetch = FetchType.LAZY)
	private List<Module> modules;

//	@ManyToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name = "parentId", insertable = false, updatable = false)
//	private Subject parentSubject;
//
//	/**
//	 * 父科目id
//	 */
//	private Long parentId;
//
//	/**
//	 *层级数
//	 */
//	private Integer layer;
}
