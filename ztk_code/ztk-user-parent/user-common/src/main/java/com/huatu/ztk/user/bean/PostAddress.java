package com.huatu.ztk.user.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户收货地址
 * Created by shaojieyue
 * Created time 2016-10-11 14:04
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class PostAddress implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private long uid;
    private String phone;//收货人手机号
    private String consignee;//收货人
    private String province;
    private String city;
    private String address;//详细地址
    private int defalut;//是否是默认 1:默认 2:不是默认
    private Date createTime;//创建时间
}
