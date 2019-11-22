package com.huatu.tiku.schedule.biz.domain;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import com.huatu.tiku.schedule.base.domain.BaseDomain;

import lombok.Getter;
import lombok.Setter;

/**
 * 直播间
 * 
 * @author Geek-S
 *
 */
@Entity
@Getter
@Setter
public class LiveRoom extends BaseDomain {

	private static final long serialVersionUID = 5730966574014090038L;

	/**
	 * 名称
	 */
	private String name;

//	/**
//	 * 关联课程明细
//	 */
//	@OneToMany(mappedBy = "liveRoom", fetch = FetchType.LAZY)
//	private List<CourseLive> courseLives;
}
