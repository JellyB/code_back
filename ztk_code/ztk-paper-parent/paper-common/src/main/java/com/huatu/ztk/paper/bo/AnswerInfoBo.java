package com.huatu.ztk.paper.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 随堂练统计数据
 * @author zhangchong
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AnswerInfoBo {

	 private int speed;//平均答题速度
	 
	 private int rcount;//答题正确数量
}
