package com.huatu.ztk.user.bean;

import java.io.Serializable;

import com.huatu.ztk.user.common.RegisterFreeCourseDetailVo;

import lombok.Data;

/**
 * 用户会话实体
 * Created by shaojieyue
 * Created time 2016-04-23 21:00
 */

@Data
public class UserSession implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private int status;//账户状态
    private String email;//邮箱
    private String mobile;//手机号
    private String nick;//用户昵称
    private String signature;//个性签名
    private String uname;//华图用户名称，带有唯一性
    private long expireTime;//过期时间
    private String token;
    private int subject;
    private String subjectName;
    private int area;
    private String areaName;//区域名字
    private String ucId;//ucenter id 目前修改为用户手机号
    private boolean audit;//判断ios是否处于beta版本 true:是
    private String avatar; //头像url
    private int qcount;//专项练习抽题的个数
    private int errorQcount;//错题练习题目的数量
    private int catgory;//考试类型
    private String regFrom;//注册来源(不存redis)

    //注册送课相关信息 不存redis 教育同步的学员不送课
    private RegisterFreeCourseDetailVo RegisterFreeCourseDetailVo;
    /**
     * 是否首次登录用来上报注册位置 教育同步的学员需要上报
     */
    private boolean firstLogin;
}
