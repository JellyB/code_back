package com.huatu.ztk.user.dubbo;

import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.user.bean.UserDto;
import com.huatu.ztk.user.bean.UserSession;

/**
 * 用户dubbo接口
 * Created by shaojieyue
 * Created time 2016-07-06 17:22
 */
public interface UserDubboService {

    /**
     * 根据id查询用户信息
     * @param uid
     * @return
     */
    public UserDto findById(long uid);

    /**
     * 通过账户密码登录
     * @param account
     * @param password
     * @param terminal
     * @return
     */
    public UserSession login(String account, String password, int terminal,String deviceToken) throws BizException;

    /**
     * 注册账户
     * @param mobile 手机号
     * @param captcha 验证码
     * @param terminal 终端
     * @param password 密码
     * @return
     */
    public UserSession register(String mobile,String captcha,String password,int terminal,String deviceToken)throws BizException;

    /**
     * 注销用户
     * @param token
     */
    public void logout(String token) throws BizException;

    /**
     * 生成session
     * @param account 账户
     * @return
     */
    public UserSession generateSession(String account,int catgory) throws BizException;
}
