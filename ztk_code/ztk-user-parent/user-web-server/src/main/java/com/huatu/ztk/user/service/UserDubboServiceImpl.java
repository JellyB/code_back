package com.huatu.ztk.user.service;

import com.huatu.ztk.commons.CatgoryType;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.user.bean.UserDto;
import com.huatu.ztk.user.bean.UserSession;
import com.huatu.ztk.user.common.UserErrors;
import com.huatu.ztk.user.dao.UserDao;
import com.huatu.ztk.user.dubbo.UserDubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by shaojieyue
 * Created time 2016-07-06 18:52
 */
public class UserDubboServiceImpl implements UserDubboService {
    private static final Logger logger = LoggerFactory.getLogger(UserDubboServiceImpl.class);
    public static final String DEFAULT_REGIP = "127.0.0.1";

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserService userService;

    /**
     * 根据id查询用户信息
     *
     * @param uid
     * @return
     */
    @Override
    public UserDto findById(long uid) {
        return userDao.findById(uid);
    }

    /**
     * 通过账户密码登录
     *
     * @param account
     * @param password
     * @param terminal
     * @return
     */
    @Override
    public UserSession login(String account, String password, int terminal,String deviceToken) throws BizException {
        return userService.login(account, password, terminal, CatgoryType.GONG_WU_YUAN,deviceToken);
    }


    /**
     * 注册账户
     *
     * @param mobile   手机号
     * @param captcha  验证码
     * @param terminal 终端
     * @param password 密码
     * @return
     */
    @Override
    public UserSession register(String mobile, String captcha, String password, int terminal,String deviceToken) throws BizException {
        return userService.register(mobile, captcha, password, DEFAULT_REGIP, CatgoryType.GONG_WU_YUAN,terminal,deviceToken, terminal);
    }

    /**
     * 注销用户
     *
     * @param token
     */
    @Override
    public void logout(String token) throws BizException {
        userService.logout(token,1);
    }

    /**
     * 生成session
     *
     * @param account 账户
     * @return
     */
    @Override
    public UserSession generateSession(String account,int catgory) throws BizException {
        if (catgory != CatgoryType.GONG_WU_YUAN && catgory !=CatgoryType.SHI_YE_DAN_WEI) {
            throw new BizException(CommonErrors.INVALID_ARGUMENTS,"非法的科目");
        }
        UserDto userDto = userDao.findAny(account);
        if (userDto == null) {//用户不存在
            throw new BizException(UserErrors.USER_NOT_EXIST);
        }
        userService.changeOldToken(userDto.getId(),UserSessionService.DEFAULT_TERMINAL);
        UserSession session = userService.createSession(userDto, catgory,UserSessionService.DEFAULT_TERMINAL);
        return session;
    }

}
