package com.huatu.ztk;

import com.huatu.ztk.commons.CatgoryType;
import com.huatu.ztk.commons.SubjectType;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.user.bean.UserDto;
import com.huatu.ztk.user.bean.UserSession;
import com.huatu.ztk.user.dubbo.UserDubboService;
import com.huatu.ztk.user.service.UserService;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by shaojieyue
 * Created time 2016-07-07 14:51
 */
public class UserDubboServiceTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(UserDubboServiceTest.class);

    @Autowired
    private UserDubboService userDubboService;

    @Autowired
    private UserService userService;

    @Test
    public void findByIdTest(){
        final UserDto userDto = userDubboService.findById(12252065);
        Assert.assertEquals(userDto.getId() ,12252065);
        Assert.assertEquals(userDto.isRobot(),false);
    }

    @Test
    public void generateSessionTest(){
        String account = "13717670214";
        UserSession session = null;
        try {
            session = userDubboService.generateSession(account, CatgoryType.GONG_WU_YUAN);

        } catch (BizException e) {
            e.printStackTrace();
            Assert.assertEquals(false,true);
        }

        final UserDto userDto = userService.findById(session.getId());
        Assert.assertEquals(userDto.getArea(),session.getArea());
        Assert.assertEquals(userDto.getEmail(),session.getEmail());
        Assert.assertEquals(userDto.getMobile(),session.getMobile());
        Assert.assertEquals(userDto.getName(),session.getUname());
        Assert.assertEquals(userDto.getSubject(),session.getSubject());
        try {
            session = userDubboService.generateSession(account, -1);
        } catch (BizException e) {
            Assert.assertEquals(CommonErrors.INVALID_ARGUMENTS.getCode(),e.getErrorResult().getCode());
        }
    }
}
