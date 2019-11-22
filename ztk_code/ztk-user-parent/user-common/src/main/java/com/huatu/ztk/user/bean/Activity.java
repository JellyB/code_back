package com.huatu.ztk.user.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 *活动
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class Activity implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;//id
    private String name;//活动名称
    private String image;//图片url
    private String link;//活动链接
    private long createTime;//创建时间
    private long beginTime;//活动开始时间
    private long endTime;//活动结束时间
    private long pv;//点击量
    private int status;//活动状态
}
