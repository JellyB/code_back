package com.huatu.tiku.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by lijun on 2018/5/31
 */
@Data
@Builder
@Table(name = "advert")
@AllArgsConstructor
@NoArgsConstructor
public class Advert implements Serializable {
    @Id
    private Integer id;

    /**
     * 图片url
     */
    private String image;

    /**
     * 广告启动图需要区分手机和平板展示
     * 平板图片url
     */
    private String padImageUrl;

    /**
     * 广告标题
     */
    private String title;

    /**
     * 跳转目的地 RedisKey.Target
     */
    private String target;

    /**
     * 跳转目的地 - 名称
     */
    @Transient
    private String targetName;

    /**
     * 参数，json形式
     */
    private String params;

    /**
     * 广告类型，RedisKey.Type
     */
    private Integer type;

    /**
     * 广告类型 - 名称
     */
    @Transient
    private String typeName;

    /**
     * 是否有效,1：有效,0:无效
     */
    private Integer status;

    /**
     * 科目,RedisKey.Catgort
     */
    @Column(name = "catgory")
    private Integer category;

    /**
     * 科目 - 名称
     */
    @Transient
    private String categoryName;

    /**
     * 是否新版,新版:1,旧版0
     */
    private Integer newVersion;

    /**
     * app类型，0：砖题库和华图在线共用，1：砖题库，2：华图在线
     */
    private Integer appType;

    /**
     * 广告序号
     */
    @Column(name = "`index`")
    private Integer index;

    /**
     * 创建时间
     */
    private Timestamp createTime;

    /**
     * 上线时间
     */
    private String onLineTime;

    /**
     * 下线时间
     */
    private String offLineTime;

    /**
     * 显示平台
     */
    private String platForm;

    /**
     * 跨科目跳转
     */
    @Column(name = "subject")
    private int subject;

    /**
     * 目标课程列表
     */
    @Column(name = "cate_id")
    private int cateId;

    @Column(name = "m_params")
    private String mParams;

    @Transient
    private int mid;

    @Transient
    private String mtitle;

    /**
     * 是否选中 app 端
     */
    @Column(name = "a")
    private int a;

    /**
     * 是否选中 m 站
     */
    @Column(name = "m")
    private int m;

    public Advert(Integer id, String image, String title, String target, String params, Integer type, Integer status, Integer category, Integer newVersion, Integer appType, Integer index, Timestamp createTime, String onLineTime, String offLineTime, String padImageUrl) {
        this.id = id;
        this.image = image;
        this.title = title;
        this.target = target;
        this.params = params;
        this.type = type;
        this.status = status;
        this.category = category;
        this.newVersion = newVersion;
        this.appType = appType;
        this.index = index;
        this.createTime = createTime;
        this.onLineTime = onLineTime;
        this.offLineTime = offLineTime;
        this.padImageUrl = padImageUrl;
    }
}
