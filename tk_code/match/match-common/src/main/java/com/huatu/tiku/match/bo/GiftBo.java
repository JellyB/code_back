package com.huatu.tiku.match.bo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-01-16 下午3:28
 **/
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.ALWAYS)
public class GiftBo {

    /**
     * 右侧
     */
    private String rightImgUrl;

    /**
     * 打开之后的礼包图片
     */
    private String giftImgUrl;

    /**
     * 活动网页地址
     */
    private String giftHtmlUrl;

    /**
     * 领取提示加群链接
     */
    private String addGroupUrl;

    /**
     * 是否已经领取过红包，默认是尚未领取过 ,1标示领取过
     */
    private int hasGetBigGift = 0;

    private Integer courseId;
}
