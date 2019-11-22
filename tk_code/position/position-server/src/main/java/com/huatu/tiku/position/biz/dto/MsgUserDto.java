package com.huatu.tiku.position.biz.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**公众号用户信息
 * @author wangjian
 **/
@Getter
@Setter
public class MsgUserDto  implements Serializable {

    private static final long serialVersionUID = 3452500416984052893L;

    private String subscribe;//是否订阅
    private String openid;//id
    private String nickname;//昵称
    private String sex;//性别
    private String city;//城市
    private String country;//国家
    private String province;//省份
    private String language;//语言
    private String headimgurl;//头像
    private String subscribe_time;//关注时间
    private String unionid;//备注
    private String remark;//备注
    private String groupid;//分组ID
    private String tagid_list;//ID列表
    private String subscribe_scene;//关注的渠道来源
    private String qr_scene;//二维码扫码场景
    private String qr_scene_str;//二维码扫码场景描述
    private String errcode;//错误状态码
    private String errmsg;//错误信息


}
