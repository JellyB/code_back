package com.huatu.tiku.essay.vo.req;

import lombok.Data;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/7/19
 * @描述 后台批改订单操作参数（撤回原因,退回学员）
 */
@Data
public class CorrectOperateRep {

    /**
     * 订单ID
     */
    private long orderId;
    /**
     * 原因ID（退回学员有枚举,其他此字段为空）
     */
    private int reasonId;

    /**
     * 操作原因
     */
    private String otherReason;

    /**
     * 老师拒绝接单会用到
     */
    private  Integer orderType;


}
