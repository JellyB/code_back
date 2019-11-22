package com.huatu.ztk.user.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 广告
 * Created by shaojieyue
 * Created time 2016-06-27 10:08
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class Advert implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;//id
    private String image;//图片地址
    private String link;//广告链接
    private String title;//标题
    private int status;//是否有效,1：有效,0:无效
    private String content;//广告内容
    private long createTime;//创建时间

}
