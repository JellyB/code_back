package com.huatu.tiku.position.biz.domain;

import com.huatu.tiku.position.base.domain.BaseDomain;
import com.huatu.tiku.position.biz.enums.*;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

/**用户
 * @author wangjian
 **/
@Entity
@Getter
@Setter
public class User extends BaseDomain {

    private static final long serialVersionUID = 1997610558729521652L;

    @Column(unique = true)
    private String openId;//微信openId
    private String unionId;//微信unionid
    private String password;//密码
    private String phone;//电话
    private String nickName;//昵称
    private String signature;//签名
    private Sex sex;//性别
    private Status status;//状态

    /**
     * 专业
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialtyId", insertable = false, updatable = false)
    private Specialty specialty;//专业
    private Long specialtyId;//专业
    private Boolean recent;//是否应届 0为否 1是
    private Integer graduationYear;//毕业年份
    private Integer birthdayYear;//出生年
    private String nation;//民族
    private Education education;//学历
    private Degree degree;//学位
    private String englishType;//英语水平类型
    private Exp exp;//工作经验
    private BaseExp baseExp;//基层工作经验
    private Political political;//政治面貌

    @ManyToMany
    @JoinTable(joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "area_id"))
    private Set<Area> areas;//报考地点

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registerAreaId", insertable = false, updatable = false)
    private Area registerArea;//户籍
    private Long registerAreaId;//户籍

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "birthAreaId", insertable = false, updatable = false)
    private Area birthArea;//出生地
    private Long birthAreaId;//出生地

    private String certificate;//证书

}
