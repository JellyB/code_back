package com.huatu.ztk.user.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户反馈
 * Created by shaojieyue
 * Created time 2016-06-06 18:05
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class Feedback implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private long uid;//反馈所属用户id
    private String content;//反馈内容
    private String contacts;//联系方式
    private String imgs; //图片
    private String log; //反馈日志
    private int type; //1：其他,4:程序bug,5:功能建议,6:内容意见 7:申论
    private long createTime;//创建时间
    private String uname;
    private String environmentMap;
    //eJyrVnIsKHje2fFszpqn/duVrJQM9Qz0DJR0lJ5N7n2yd86LdfueLmkHCudmAsWeb979fPd8ZNW5maWZSrUAlb0iDg==
    //
    private String appVersion;
    //设备
    private String facility;
    private String sysVersion;

    //回复次数
    private int replyNum;
    private String msgIds;
    private int isSolve;

    //更新人
    private String modifier;
    private long modifyTime;
    //消息推送
    private String pushLog;

}
