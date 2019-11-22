package com.huatu.ztk;

import com.huatu.ztk.user.bean.UserSign;
import com.huatu.ztk.user.dao.UserSignDao;
import com.huatu.ztk.user.service.UserRewardService;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * Created by linkang on 2017/10/16 下午6:43
 */
public class UserRewardServiceTest extends BaseTest{
    @Autowired
    private UserRewardService userRewardService;

    @Autowired
    private UserSignDao userSignDao;

    @Test
    public void sendSignMessage() throws Exception {

        Date date = DateUtils.parseDate("20170901", "yyyyMMdd");
        for (int i = 0; i < 30; i++) {
            userRewardService.sign(239, "hehe", DateUtils.addDays(date, i));
        }


//        userRewardService.sign(239, "hehe", new Date());

    }

    @Test
    public void sendRegisterMessage() throws Exception {

    }

    @Test
    public void findTodaySign() throws Exception {
        UserSign todaySign = userRewardService.findTodaySign(239);

        Assert.assertNotNull(todaySign);
    }

}