package com.huatu.ztk.backend.filter;

import com.huatu.ztk.backend.user.bean.User;
import com.huatu.ztk.backend.user.dao.UserDao;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Author: xuhuiqiang
 * Time: 2017-01-10  16:40 .
 */
public class MyRealm extends AuthorizingRealm {


    @Autowired
    private UserDao userDao;

    /**
     * 授权信息
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(
            PrincipalCollection principals) {
        return null;
    }

    /**
     * 认证信息
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(
            AuthenticationToken authcToken ) throws AuthenticationException {
        UsernamePasswordToken token = (UsernamePasswordToken) authcToken;
        String userName = token.getUsername();
        String userPassword =  String.valueOf(token.getPassword());
        System.out.println("用户名："+userName+"   密码："+userPassword);
        if( userName != null && !"".equals(userName) ){
            User user = userDao.findByAccount(userName);
            if (user == null) {//账户不存在
                return null;
            }
            /**
             * $md5 = md5($_POST['finalPassword']);//初次加密
             $slat_code = substr($md5, -6, 4);//$salt

             $_POST['finalPassword'] = md5(hash('sha256',$slat_code).$_POST['finalPassword']);
             */
            final String tmp = Hex.encodeHexString((DigestUtils.getMd5Digest().digest(userPassword.getBytes())));
            String slat = tmp.substring(tmp.length()-6,tmp.length()-6+4);
            final String shaSlat = Hex.encodeHexString(DigestUtils.getSha256Digest().digest(slat.getBytes()));
            final String finalPassword = Hex.encodeHexString(DigestUtils.getMd5Digest().digest((shaSlat +userPassword).getBytes()));
            //92e179e3f8bf1bf30c8497704f9a326b
            if (!user.getPassword().equals(finalPassword)) {//密码不正确也返回null 说明登录失败
                return null;
            }
            return new SimpleAuthenticationInfo(
                    user.getAccount(),user.getPassword(), getName());
        }//返回用户基本信息


        return null;
    }
}
