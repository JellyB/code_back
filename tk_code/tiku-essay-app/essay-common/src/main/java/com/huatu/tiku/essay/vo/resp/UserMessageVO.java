package com.huatu.tiku.essay.vo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by x6 on 2017/12/5.
 * 用户意见反馈回复VO
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserMessageVO {
    private long id;//id
    private long uid;//信息所属用户
    private String title;//消息标题
    private String content;//消息内容
    private int type;//消息类型
    private int status;//消息读取状态
    private long createTime;//创建时间
}
