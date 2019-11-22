package com.huatu.tiku.essay.vo.req;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 修改个人设置请求数据
 * 
 * @author zhangchong
 *
 */
@Data
@NoArgsConstructor
public class TeacherOrderTypeReq {

	private Long id;

	/**
	 * 1.小题 2.议论文 3.应用文 4.套题
	 */
	private Integer orderType;

	/**
	 * 接单状态 1可接单 0不可接单
	 */
	private Integer receiptStatus;

	/**
	 * 接单限制
	 */
	private Integer orderLimit;

}
