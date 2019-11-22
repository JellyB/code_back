package com.huatu.tiku.essay.vo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by x6 on 2017/12/7.
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EssayCorrectGoodsVO {

    private Long id;
    private String name; //批改商品名称
    private int type; //批改类型（0单题批改 1多题批改 2议论文（7.0将议论文单独提出来了））
    private int inventory;//（库存）可售数量
    private int salesNum;  //已售数量
    private int price;//价格
    private int num;//商品对应批改次数
    private int activityPrice; //活动价格

    private int correctMode; //批改商品类型（1智能批改 2人工批改）
    private int isLimitNum;
    //是否有有效期(0无有效期 1  有有效期)
    private int expireFlag;
    private int expireDate; //有效期时间（天）
    private int sort;// 排序标识（标准答案 套题批改，议论文批改的顺序）

    /**
     * 销售类型
     */
    private Integer saleType;

    private double doublePrice;//价格，即前端划掉的价格(单位：元)

    private double doubleActivityPrice;//活动价格，即前端展示的价格（单位：元）

    private int bizStatus;
}
