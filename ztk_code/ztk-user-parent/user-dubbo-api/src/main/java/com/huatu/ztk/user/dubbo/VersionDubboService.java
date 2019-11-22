package com.huatu.ztk.user.dubbo;

import com.huatu.ztk.user.bean.AppVersion;
import com.huatu.ztk.user.bean.AppVersionBean;

/**
 * 版本dubbo接口
 * Created by linkang on 9/28/16.
 */
public interface VersionDubboService {

    /**
     * 获得版本信息
     * @param terminal 终端
     * @param currentVersion 当前客户端版本
     * @param userId 用户id
     * @param catgory 科目
     * @return
     */
    public AppVersionBean checkVersion(int terminal, String currentVersion, long userId,int catgory);

    /**
     * 获取指定终端下的最新版本
     *
     * @param terminal {@link com.huatu.ztk.commons.TerminalType}
     * @param catgory 科目
     * @return
     */
    public AppVersion getLatestVersion(int terminal,int catgory);
}
