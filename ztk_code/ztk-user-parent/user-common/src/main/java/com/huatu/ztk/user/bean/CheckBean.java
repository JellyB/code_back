package com.huatu.ztk.user.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by linkang on 9/21/16.
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class CheckBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private AppVersionBean appVersionBean; //app更新检查bean
    private int commentStatus; //好评送课开关
    private boolean audit; //是否审核版本
    private String aboutPhone; //关于页面联系方式
    private String aboutEmail; //课程页面联系方式
    private String coursePhone; //课程详情联系方式
    private String seckillUrl;//秒杀url

    private int essayCorrectFree;//申论批改是否免费
    private int photoAnswer;//是否支持拍照答题（0支持  1不支持）
    private int voiceAnswer;//是否支持语音答题（0支持  1不支持）

    private int photoAnswerType;//拍照识别对接第三方（0  汉王  1优图）
    private String photoAnswerMsg;//拍照答题的提示msg
    private int fur;//是否是广告白名单用户  1 是白名单用户,0非白名单

}
