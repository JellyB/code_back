package com.huatu.ztk.dao;

import com.huatu.ztk.BaseTest;
import com.huatu.ztk.user.bean.AppVersion;
import com.huatu.ztk.user.dao.AppVersionDao;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by shaojieyue
 * Created time 2016-11-21 19:22
 */
public class AppVersionDaoTest extends BaseTest{

    @Autowired
    private AppVersionDao appVersionDao;

    @Test
    public void findLatestVersionTest(){
        final AppVersion latestVersion = appVersionDao.findLatestVersion(2,1);
        Assert.assertEquals(latestVersion.getMessage(),"2.1.1");
        Assert.assertEquals(latestVersion.getBulk(),"https://my.oschina.net/blackylin/blog/144136");
        Assert.assertEquals(latestVersion.getBulkMd5(),"jfaojesflanmsldfjowjeflansldfjoaiwenmflasf");
        Assert.assertEquals(latestVersion.getFull(),"https://ns.huatu.com/q/v1/questions");
        Assert.assertEquals(latestVersion.getLevel(),1);
        Assert.assertEquals(latestVersion.getVersion(),"2.1.1");
        Assert.assertEquals(latestVersion.getVersionCount(),211);
    }

    @Test
    public void findVersionTest(){
//        final AppVersion version = appVersionDao.findVersion(2, 210);
//        Assert.assertEquals(version.getMessage(),"2.1.0");
//        Assert.assertEquals(version.getVersionCount(),210);
    }
}
