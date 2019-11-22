package com.huatu.tiku.position.biz.vo;

import com.huatu.tiku.position.biz.enums.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author wangjian
 **/
@Getter
@Setter
public class UserVo implements Serializable {
    private static final long serialVersionUID = 8990082123525374379L;

    private Long id;
    private String phone;//电话
    private String nickName;//昵称
    private String signature;//签名
    private Sex sex;//性别
    private Status status;//状态

    private String specialty;//专业
    private Long specialtyId;//专业
    private Boolean recent;//是否应届 0为否 1是
    private Integer graduationYear;//毕业年份
    private Integer birthdayYear;//出生年
    private String nation;//民族
    private Education education;//学历
    private Degree degree;//学位
    private List<String> englishTypes;//英语水平类型
    private Exp exp;//工作经验
    private BaseExp baseExp;//基层工作经验
    private Political political;//政治面貌

    private List areas;//报考地点

    private String registerArea;//户籍
    private Long registerAreaId;//户籍

    private String birthArea;//出生地
    private Long birthAreaId;//出生地

    private List<String> certificates;//证书
}
