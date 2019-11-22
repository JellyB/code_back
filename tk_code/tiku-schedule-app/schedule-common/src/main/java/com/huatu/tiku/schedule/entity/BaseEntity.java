package com.huatu.tiku.schedule.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@MappedSuperclass
@DynamicInsert
@DynamicUpdate(true)
public class BaseEntity {

	@Id
	@GeneratedValue
	protected Long id;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(updatable = false)
	@org.hibernate.annotations.CreationTimestamp
	protected Date gmtCreate;

	@org.hibernate.annotations.UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	protected Date gmtModify;

}
