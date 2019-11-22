package com.huatu.tiku.course.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * @author hanchao
 * @date 2017/9/13 18:20
 */
@Data
public class CouponV3DTO implements Serializable{
    private static final long serialVersionUID = 1L;
    /**
     * par : 10
     * price : 100
     * voucherid : JFDH10
     * endtime : 30
     * integral_num : 100
     */

    private int par;
    private int price;
    private String voucherid;
    private int endtime;
    private int integral_num;
    private int sales;
}
