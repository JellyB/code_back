package com.huatu.tiku.course.test;

import com.huatu.common.test.BaseTest;
import com.huatu.tiku.course.service.v1.UserAccountService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-03-06 3:52 PM
 **/
@Slf4j
public class UserAccountServiceTest extends BaseTest{

    @Autowired
    private UserAccountService userAccountService;

    @Test
    public void test(){
        String userName = "app_ztk802796288";
        Long userId = userAccountService.userId(userName);
        log.info("userId:{}", userId.toString());
    }
}
