package com.huatu.ztk.backend.version.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created by shaojieyue
 * Created time 2016-11-21 16:48
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class AppVersion {
    private int id;
    private int level;//更新等级 1:普通 2:强制更新
    private String version;//版本号，如2.0.3
    private String message;//提示语
    private String full;//全量地址
    private String bulk;//增量地址
    private String bulkMd5;//增量文件md5值
    private int versionCount; //如203
    private int updateMode;//更新比例,取模的值
    private int client; //终端类型,1：android，2：ios
    private Date createTime; //创建时间
    private int catgory; //科目
}
