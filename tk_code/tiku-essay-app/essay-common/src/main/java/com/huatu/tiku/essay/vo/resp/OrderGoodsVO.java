package com.huatu.tiku.essay.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by x6 on 2017/11/28.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderGoodsVO {

    /**
     * 商品id
     */
    private Long goodsId;
    /**
     * 商品名称
     */
    private String name;
    /**
     * 商品数量
     */
    private Integer count;

    private Integer correctMode;

    private Integer expireDate;

    private Integer expireFlag;
    /**
     * 订单详情
     */
    private String memo;



}
