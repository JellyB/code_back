package com.huatu.tiku.essay.vo.admin;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by duanxiangchao on 2019/7/29
 */
@Data
public class FeedBackStatisticVO {

    private Long star;

    private Long ids;

    public FeedBackStatisticVO(Long star, Long ids){

        this.ids = ids;
        this.star = star;

    }

}
