package com.huatu.ztk.backend.advert.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Created by renwenlong on 2016/11/16.
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class Advert {

    private long id;
    private String image;//图片url
    private String title;//广告标题
    private String target;//跳转目的地
    private String params;//参数，json形式
    private int index;//广告序号
    private int type;//广告类型，1:首页轮播图 2:启动页图片 3:首页弹出图
    private int status;//是否有效,1：有效,0:无效
    private int catgory;//科目,默认公考,1:公考,2:教师考试,3:事业单位
    private Timestamp createTime;//创建时间
    private int newVersion; //是否新版,新版:1,旧版0
    private int appType;
    /**
     * 上线时间
     */
    private Long onlineTime;
    /**
     * 下线时间
     */
    private Long offlineTime;
}
