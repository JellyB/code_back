package com.huatu.ztk.user.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @创建人 lizhenjuan
 * @创建时间 2018/8/1
 * @描述
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class AppChannel implements Serializable {

    private long id;
    //idfa 值，设备唯一ID
    private String deviceToken;
    //创建时间
    private Long gmtCreate;
    //ip地址
    private String ip;
    //渠道号
    private int source;
    //状态（1未激活；2激活）
    private int state;
    //创建时间
    private Timestamp createTime;
    //激活时间
    private Timestamp updateTime;
    //第三方回调参数
    private String callBack;
    //区分ios跟安卓渠道
    private int sourceType;
    //系统版本
    private String version;
    //手机型号
    private String model;
    //app版本号
    private String cv;
    //是否是越狱设备
    private int isBreakPrison;
}
