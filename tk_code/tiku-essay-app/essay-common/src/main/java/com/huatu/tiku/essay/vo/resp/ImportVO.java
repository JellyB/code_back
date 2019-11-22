package com.huatu.tiku.essay.vo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by x6 on 2018/2/8.
 * 买课赠送批改次数
 *
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class ImportVO {

    //对应行数
    private int index;
    //订单id
    private int orderId;
    //用户名称
    private String userName;
    //课程id
    private long productId;
    //手机号
    private String phone;
    //订单id
    private String strOrder;
}
