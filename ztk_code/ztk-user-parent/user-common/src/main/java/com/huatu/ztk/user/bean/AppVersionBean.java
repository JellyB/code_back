package com.huatu.ztk.user.bean;

import com.sun.javafx.geom.PickRay;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 *
 * app更新检查bean
 * Created by shaojieyue
 * Created time 2016-07-01 14:30
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class AppVersionBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean update;//是否更新
    private int level;//更新等级 1:普通 2:强制更新
    private String latestVersion;//最新版本
    private String message;//提示语
    private String full;//全量地址
    private String bulk;//增量地址
    private String bulkMd5;//增量文件md5值
    private int updateChannel;//渠道包 全部 小米 豌豆荚
    private String patchUrl;//补丁地址
    private String patchMd5;//补丁md5
    private Boolean isPatch;//是否是补丁
}
