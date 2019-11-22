package com.huatu.ztk.user.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by shaojieyue
 * Created time 2016-06-16 20:22
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class UserMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;//id
    private long uid;//信息所属用户
    private String title;//消息标题
    private String content;//消息内容
    private int type;//消息类型
    private int status;//消息读取状态
    private long createTime;//创建时间
}
