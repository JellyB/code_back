package com.huatu.ztk.paper.bean;

import lombok.*;

/**
 * Created by lijun on 2018/10/18
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@ToString(callSuper = true)
public class AnswerCardWithGift extends StandardCard {


    private String rightImgUrl;//右侧
    private String giftImgUrl;//打开之后的礼包图片
    private String giftHtmlUrl;//活动网页地址
    private String addGroupUrl; //领取提示加群连接
    private int hasGetBigGift = 0;//是否已经领取过红包，默认是尚未领取过 ,1标示领取过
    private Integer courseId;       //课程ID


    public AnswerCardWithGift(StandardCard standardCard) {
        super(standardCard.getPaper(), standardCard.getCardUserMeta(), standardCard.getMatchMeta(), standardCard.getIdStr(), standardCard.getCurrentTime());
    }
}
