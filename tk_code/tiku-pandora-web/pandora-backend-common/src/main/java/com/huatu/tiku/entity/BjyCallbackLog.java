package com.huatu.tiku.entity;

import javax.persistence.Table;

import com.huatu.common.bean.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 百家云callback回调日志
 * 
 * @author zhangchong
 *
 */
@Data
@NoArgsConstructor
@Table(name = "bjy_callback_log")
@AllArgsConstructor
@Builder
public class BjyCallbackLog extends BaseEntity {

	/**
	 * 百家云 roomID
	 */
	private Long roomId;

	/**
	 * 类型
	 */
	private String op;

	/**
	 * 操作时间
	 */
	private String opTime;

	/**
	 * 请求唯一标示
	 */
	private String qid;

	private Integer timestamp;

	private String sign;

}
