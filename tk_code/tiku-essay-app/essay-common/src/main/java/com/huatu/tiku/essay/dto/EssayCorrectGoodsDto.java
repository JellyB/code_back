package com.huatu.tiku.essay.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-07-15 2:42 PM
 **/

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EssayCorrectGoodsDto {

    private long id;
    protected int bizStatus;
    protected int status = 1;
    protected String creator;
    protected String modifier;
    protected Date gmtCreate;
    protected Date gmtModify;
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
    private int expireDate; //有效期时间
    private int sort;// 排序标识（标准答案 套题批改，议论文批改的顺序）

    /**
     * 售卖类型
     */
    private Integer saleType;
}
