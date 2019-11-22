package com.huatu.tiku.essay.vo.admin.correct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单简单封装
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CorrectOrderSimpleVO {

    /**
     * 到期时间
     */
    private long expireTime;

    /**
     * 订单ID
     */
    private long orderId;


    private long receiveOrderTeacher;
}
