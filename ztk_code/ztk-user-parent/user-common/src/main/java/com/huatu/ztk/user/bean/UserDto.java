package com.huatu.ztk.user.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.io.Serializable;


/**
 * 用户dto
 * Created by shaojieyue
 * Created time 2016-04-24 09:48
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class UserDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;//用户id
    private String mobile;//手机号
    private String email;//邮箱
    private String name;//用户名,该字段不要更改,跟网校有联系
    private String nick;//昵称,显示用户姓名用这个字段
    private String signature;//个性签名
    private int area;//用户区域id
    private int subject;//用户练习的科目
    private int status;//用户状态
    private String avatar;//头像地址 例如：http://xxx/header.png
    private String regFrom;//注册来源
    private String deviceToken;
    @Getter(onMethod = @__({ @JsonIgnore }))
    private boolean isRobot;//该用户是否是机器人,竞技场在用
    @Getter(onMethod = @__({ @JsonIgnore }))
    private long createTime;//创建时间
    @Getter(onMethod = @__({ @JsonIgnore }))
    private long mobileUserId;//对应的移动端用户id
    //@Getter(onMethod = @__({ @JsonIgnore }))
    private long ucenterId;
    @Getter(onMethod = @__({ @JsonIgnore }))
    private String nativepwd;//原生的密码,该字段是临时的
    @Getter(onMethod = @__({ @JsonIgnore }))
    private String password;


}
