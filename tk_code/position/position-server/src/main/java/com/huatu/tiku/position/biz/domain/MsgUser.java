package com.huatu.tiku.position.biz.domain;

import com.huatu.tiku.position.base.domain.BaseDomain;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * @author wangjian
 **/
@Getter
@Setter
@Entity
public class MsgUser extends BaseDomain {

    private static final long serialVersionUID = 412552842196395326L;

    private String subscribe;//是否订阅
    @Column(unique = true)
    private String openid;//id
    private String nickname;//昵称
    private String sex;//性别
    private String city;//城市
    private String country;//国家
    private String province;//省份
    private String language;//语言
    private String headimgurl;//头像
    private String subscribe_time;//关注时间
    @Column(unique = true)
    private String unionid;
    private String remark;//备注
    private String groupid;//分组ID
    private String tagid_list;//ID列表
    private String subscribe_scene;//关注的渠道来源
    private String qr_scene;//二维码扫码场景
    private String qr_scene_str;//二维码扫码场景描述
}
