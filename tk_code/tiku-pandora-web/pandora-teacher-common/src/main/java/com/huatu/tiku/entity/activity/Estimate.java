package com.huatu.tiku.entity.activity;

import com.huatu.common.bean.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * @创建人 lizhenjuan
 * @创建时间 2018/10/19
 * @描述
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "estimate_activity")
public class Estimate extends BaseEntity {

    /**
     * 精准估分列表图标
     */
    @NotNull(message = "列表图标不能为空")
    private String iconUrl;
    /**
     * 活动图标  精准估分提交答题卡后右侧图标
     */
    @NotNull(message = "活动图标不能为空")
    private String rightImgUrl;
    /**
     * 大礼包图片
     */
    @NotNull(message = "礼包图片不能为空")
    private String giftImgUrl;

    /**
     * 锦鲤包(专题链接)
     */
    @NotNull(message = "分数线之上的礼包地址不能为空")
    private String upGiftHtmlUrl;


    /**
     * 加油包（专题链接）
     */
    @NotNull(message = "分数之下的礼包地址不能为空")
    private String downGiftHtmlUrl;

    /* *//**
     * 加油包（专题链接）分数线之下
     *//*
    private String downGiftHtmlUrlPc;*/


    /**
     * 尚未领取链接
     */
    private String notGetBagUrl;


    /**
     * 已经领取链接
     */
    private String hasGetBagUrl;

    /**
     * 分数线
     */
    @NotNull(message = "分数线不能为空")
    private Double score;

    /**
     * 精准估分活动ID
     */
    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    private String addGroupUrl;

    /*  *//**
     * 课程ID
     *//*
    private Integer courseId;
*/
    /**
     * 活动类型（模考大赛or 精准估分）
     */
    @NotNull
    private Integer type;

    /**
     * 分数之上的课程ID
     */
    @NotNull(message = "分数之上的课程ID不能为空")
    private Integer upCourseId;


    /**
     * 课程之下的课程ID
     */
    @NotNull(message = "分数之下的课程ID不能为空")
    private Integer downCourseId;


    @NotNull(message = "二维码不能为空!")
    private String qrCodeImageUrl;

}