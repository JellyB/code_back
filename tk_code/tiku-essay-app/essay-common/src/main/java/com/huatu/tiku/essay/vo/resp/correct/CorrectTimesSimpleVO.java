package com.huatu.tiku.essay.vo.resp.correct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author huangqingpeng
 * @title: CorrectTimesSimpleVO
 * @description: 用户某个类型批改次数返回值
 * @date 2019-07-0821:52
 */
@AllArgsConstructor
@Builder
@Data
public class CorrectTimesSimpleVO {
    /**
     * 商品（批改）类型
     */
    private Integer goodsType;
    /**
     * 商品名称
     */
    private String goodsName;
    /**
     * 是否限制次数（0 无限制次数 1 限制次数）
     */
    private int isLimitNum;
    /**
     * 总次数
     */
    private int num;

    /**
     * 即将过期的次数
     */
    private int willExpireNum;

    /**
     * 带限定条件的次数（试卷限定次数）
     */
    private int specialNum;


}
