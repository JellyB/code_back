package com.huatu.ztk.pc.bean;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.huatu.ztk.paper.bean.AnswerCard;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Map;

/**
 *
 * 分享bean
 * Created by shaojieyue
 * Created time 2016-09-18 15:12
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
@Document(collection = "ztk_share")
public class Share implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String id; //分享的id
    private String title;//分享的标题
    private String desc;//分享详细描述
    private String url;//分享跳转url
    @Getter(onMethod = @__({ @JsonIgnore }))
    private String outerId;//外部数据id
    @Getter(onMethod = @__({ @JsonIgnore }))
    private int type;//分享类型
    //新加报告页面数据（新版模考大赛用）
    private Map<String,Object> reportInfo;
    //分享练习专用
    private AnswerCard   answerCard;
    
    //音频分享所有数据
    private Map<String,Object> videoInfo;
    //微信小程序分享头像和用户昵称
    private Map<String,Object> wechatInfo;
    
    
}
