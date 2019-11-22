package com.huatu.ztk;

import com.huatu.ztk.user.service.UserSessionService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by shaojieyue
 * Created time 2016-07-01 10:45
 */
public class UserSessionServiceTest extends BaseTest{
    @Autowired
    private UserSessionService userSessionService;

    @Test
    public void userTest(){
        String token = "06468a6bb89c41f1a5b3c426972963ed";
        final String mobileNo = userSessionService.getMobileNo(token);
        Assert.assertEquals(mobileNo,"13717670214");

        Assert.assertEquals(userSessionService.getArea(token),-9);
        Assert.assertEquals(userSessionService.getSubject(token),1);
        Assert.assertEquals(userSessionService.getNick(token),"137****0214");
        Assert.assertEquals(userSessionService.getUid(token),12252065);
    }
}
