package com.huatu.tiku.essay.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created by x6 on 2017/11/27.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EssayGoodsOrderVO {
    private  Long id; //用户id
    private Integer totalMoney;//支付金额
    private Integer realMoney;//支付金额
    private Integer payType;//支付类型(0 支付宝  1微信  2金币   3活动赠送)
    private  Long userId; //用户id
    private  String comment; //备注信息
    /**  收支类型  0收入  1支出 **/
    private Integer incomeType;
    private  String payMsg; //支付信息（-1000金币）
    private Date payTime;



}
