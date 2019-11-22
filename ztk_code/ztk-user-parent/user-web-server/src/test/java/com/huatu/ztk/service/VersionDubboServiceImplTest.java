package com.huatu.ztk.service;

import com.huatu.ztk.BaseTest;
import com.huatu.ztk.user.bean.AppVersion;
import com.huatu.ztk.user.bean.AppVersionBean;
import com.huatu.ztk.user.dubbo.VersionDubboService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by shaojieyue
 * Created time 2016-11-21 19:30
 */
public class VersionDubboServiceImplTest extends BaseTest{

    @Autowired
    private VersionDubboService versionDubboService;

    @Test
    public void getLatestVersionTest(){
        int terminal = 1;
        int uid = 3;
        AppVersion latestVersion = versionDubboService.getLatestVersion(terminal,1);
        Assert.assertNotNull(latestVersion);
        latestVersion = versionDubboService.getLatestVersion(-1,1);
        Assert.assertNull(latestVersion);
    }

    @Test
    public void checkVersionTest(){
        int terminal = 1;
        String currentVersion="2.2";
        int subject=3;
        long uid = 10264614;
        AppVersionBean appVersion=versionDubboService.checkVersion(terminal,currentVersion,uid,subject);
        Assert.assertNull(appVersion);
    }
}
