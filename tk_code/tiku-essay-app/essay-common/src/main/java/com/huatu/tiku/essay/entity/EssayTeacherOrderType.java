package com.huatu.tiku.essay.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * Created by duanxiangchao on 2019/7/10
 */
@Entity
@Data
@Builder
@Table(name = "v_essay_teacher_order_type")
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@DynamicInsert
public class EssayTeacherOrderType extends BaseEntity implements Serializable {

	private Long teacherId;

	/**
	 * 1.小题 2.议论文 3.应用文 4.套题
	 */
	private Integer orderType;

	/**
	 * 接单状态 1可接单 0不可接单
	 */
	private Integer receiptStatus;
	
	/**
	 * 管理员设置的最大批改量
	 */
	private Integer maxOrderLimit;

	/**
	 * 个人设置的接单限制
	 */
	private Integer orderLimit;

	/**
	 * 总批改量
	 */
	private Integer orderAmount;

	/**
	 * 当天完成批改量
	 */
	private Integer currentFinishAmount;

	/**
	 * 派单比例
	 */
	private Integer receiptRate;

	/**
	 * 派单量
	 */
	private Integer dispatchAmount;

	/**
	 * 清空当天任务量的时间
	 */
	@Temporal(TemporalType.TIMESTAMP)
	protected Date gmtClear;
}
