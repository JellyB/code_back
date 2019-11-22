package com.huatu.tiku.entity;


import lombok.*;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;


@Getter
@Setter
@NoArgsConstructor
@Table(name = "app_version")
public class AppVersion implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * app 华图在线1  面酷2
     */
    private Integer appName;

    /**
     * 终端类型 1 安卓 2 ios
     */
    private Integer terminal;

    /**
     * app 版本
     */
    private String appVersion;

    /**
     * 版本号数字值
     */
    private Integer versionCount;

    /**
     * 升级方法 提示升级 1 强制升级 2 补丁包 3
     */
    private Integer updateType;

    /**
     * 渠道 全部1 小米2 豌豆荚  3
     */
    private Integer updateChannel;

    /**
     * 升级文案
     */
    private String message;

    /**
     * 安卓 file 地址 或 ios url 地址
     */
    private String fileOrUrl;

    /**
     * 增量更新文件的md5值
     */
    private String fileMd5;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 数据状态 1、可用； 2、删除
     */
    private Integer status;

    /**
     * 灰度发布 1 全部用户 2 白名单 3 取模随机
     */
    private Integer releaseType;

    /**
     * 取模随机值
     */
    private Integer updateMode;

    @Builder
    public AppVersion(Long id, Integer appName, Integer terminal, String appVersion, Integer versionCount, Integer updateType, Integer updateChannel, String message, String fileOrUrl, String fileMd5, Date createTime, Integer status, Integer releaseType, Integer updateMode) {
        this.id = id;
        this.appName = appName;
        this.terminal = terminal;
        this.appVersion = appVersion;
        this.versionCount = versionCount;
        this.updateType = updateType;
        this.updateChannel = updateChannel;
        this.message = message;
        this.fileOrUrl = fileOrUrl;
        this.fileMd5 = fileMd5;
        this.createTime = createTime;
        this.status = status;
        this.releaseType = releaseType;
        this.updateMode = updateMode;
    }
}