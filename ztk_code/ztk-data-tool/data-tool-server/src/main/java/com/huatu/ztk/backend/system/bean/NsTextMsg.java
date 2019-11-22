package com.huatu.ztk.backend.system.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by ht on 2016/11/18.
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class NsTextMsg {
    private int id;
    private int msgId;  //信息id
    private String msgType;//消息类型
    private int useType; //使用类型
    private String title ;//标题
    private String content;//内容
    private  String createTime; //创建时间
    private String  lasteditTime;//最后更新时间
    private String deadLine;   //x消息到期时间

    private int catgory;  //1：公务员，3：事业单位
}
