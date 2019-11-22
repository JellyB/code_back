package com.huatu.ztk.common;

import com.huatu.ztk.commons.Area;
import com.huatu.ztk.commons.AreaConstants;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by shaojieyue
 * Created time 2016-12-20 14:32
 */
public class AreaConstantsTest {

    @Test
    public void getFullAreaNmaeTest(){
        String fullAreaNmae = AreaConstants.getFullAreaNmae(-9);
        Assert.assertEquals("全国",fullAreaNmae);
        fullAreaNmae = AreaConstants.getFullAreaNmae(2561);
        Assert.assertEquals("贵州毕节地区",fullAreaNmae);
        fullAreaNmae = AreaConstants.getFullAreaNmae(2106);
        Assert.assertEquals("广西",fullAreaNmae);
        fullAreaNmae = AreaConstants.getFullAreaNmae(-123);
        Assert.assertEquals("未知区域",fullAreaNmae);
    }

    @Test
    public void getAreasTest(){
        List<Area> areas = AreaConstants.getAreas(1);
        Assert.assertEquals(areas.size(),32);
        Assert.assertNull(areas.get(0).getChildren());

        areas = AreaConstants.getAreas(2);
        Assert.assertEquals(areas.size(),32);
        Assert.assertNotNull(areas.get(0).getChildren());

        areas = AreaConstants.getAreas(3);
        Assert.assertEquals(areas.size(),0);
    }
}
