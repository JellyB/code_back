package com.huatu.ztk.user.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by shaojieyue
 * Created time 2016-11-21 16:48
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class AppVersion implements Serializable {
    private static final long serialVersionUID = 1L;

    private int level;//更新等级 1:普通 2:强制更新
    private String version;//最新版本
    private String message;//提示语
    private String full;//全量地址
    private String bulk;//增量地址
    private String bulkMd5;//增量文件md5值
    private int versionCount;
    private int updateMode;//更新比例,取模的值
}
