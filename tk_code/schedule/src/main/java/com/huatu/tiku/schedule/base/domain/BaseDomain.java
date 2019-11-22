package com.huatu.tiku.schedule.base.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Getter;
import lombok.Setter;

/**
 * 实体基类
 * 
 * @author geeks
 *
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseDomain implements Serializable {

	private static final long serialVersionUID = 1359052286325363912L;

	/**
	 * 主键
	 */
	@Id
	@GeneratedValue
	private Long id;

	/**
	 * 创建时间
	 */
	@Column(updatable = false)
	private Date createTime;

	/**
	 * 更新时间
	 */
	private Date updateTime;

	/**
	 * 创建人
	 */
	@CreatedBy
	private Long createdBy;

	/**
	 * 最后更新人
	 */
	@LastModifiedBy
	private Long lastModifiedBy;

	/**
	 * 创建时间
	 */
	@CreatedDate
	private Date createdDate;

	/**
	 * 修改时间
	 */
	@LastModifiedDate
	private Date lastModifiedDatet;

}
