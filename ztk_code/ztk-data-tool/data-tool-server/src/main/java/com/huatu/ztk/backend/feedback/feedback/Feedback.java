package com.huatu.ztk.backend.feedback.feedback;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by shaojieyue
 * Created time 2016-11-14 16:40
 */



@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class Feedback {
    private long id;
    private String content;//反馈内容
    private String title;//标题
    private int catgory; //科目
    private String contact;//反馈人联系方式
    private String appVersion;//app 版本号
    private String device;//设备
    private String system;//操作系统
    private long uid;//反馈人
    private String uname;//反馈人用户名
    private long createTime;
    private int type;
}
