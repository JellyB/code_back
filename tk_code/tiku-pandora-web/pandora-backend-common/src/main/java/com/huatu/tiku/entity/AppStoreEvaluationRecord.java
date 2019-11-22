package com.huatu.tiku.entity;

import javax.persistence.Table;

import com.huatu.common.bean.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * appstore用户好评记录
 * @author zhangchong
 *
 */
@Data
@NoArgsConstructor
@Table(name = "app_store_evaluation_record")
@AllArgsConstructor
@Builder
public class AppStoreEvaluationRecord extends BaseEntity {

	/**
	 * 用户id
	 */
	private Integer userId;

	/**
	 * 用户名
	 */
	private String userName;


}
