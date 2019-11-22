package com.huatu.tiku.essay.entity;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Created by x6 on 2018/2/9.
 */
@Builder
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Table(name="v_essay_error_user")
@DynamicUpdate
@DynamicInsert
public class ErrorUser extends BaseEntity implements Serializable {

    //订单id
    private int orderId;
    //用户名称
    private String userName;
    //用户名称
    private String phone;
    //批改次数
    private int count;
    //订单id
    private String strOrder;
}
