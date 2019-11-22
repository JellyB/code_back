package com.huatu.tiku.essay.entity;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 *
 * @author x6
 * @date 2017/11/26
 * 订单支付详情表
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="v_essay_pay_detail")
@EqualsAndHashCode(callSuper = false)
@DynamicUpdate
@DynamicInsert
public class EssayPayDetail  extends BaseEntity  implements Serializable {

    /**  订单id  **/
    private long orderId;
    /**  支付方式  **/
    private int payType;
    /**  支付金额  **/
    private int payMoney;
    /**  用户id  **/
    private int userId;
    /**  收支类型  0收入  1支出 **/
    private int incomeType;


}
