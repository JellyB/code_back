package com.huatu.ztk.backend.user.service;

import com.huatu.ztk.backend.system.bean.Catgory;
import com.huatu.ztk.backend.system.dao.RoleManageDao;
import com.huatu.ztk.backend.user.bean.User;
import com.huatu.ztk.backend.user.dao.UserDao;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Created by shaojieyue
 * Created time 2016-11-04 16:15
 */

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private RoleManageDao roleDao;

    public User login(String account, String password, HttpServletRequest request) {
        User user = userDao.findByAccount(account);
        if (user == null) {//账户不存在
            return null;
        }
        /**
         * $md5 = md5($_POST['finalPassword']);//初次加密
         $slat_code = substr($md5, -6, 4);//$salt

         $_POST['finalPassword'] = md5(hash('sha256',$slat_code).$_POST['finalPassword']);
         */
        final String tmp = Hex.encodeHexString((DigestUtils.getMd5Digest().digest(password.getBytes())));
        String slat = tmp.substring(tmp.length()-6,tmp.length()-6+4);
        final String shaSlat = Hex.encodeHexString(DigestUtils.getSha256Digest().digest(slat.getBytes()));
        final String finalPassword = Hex.encodeHexString(DigestUtils.getMd5Digest().digest((shaSlat +password).getBytes()));
        //92e179e3f8bf1bf30c8497704f9a326b
        if (!user.getPassword().equals(finalPassword)) {//密码不正确也返回null 说明登录失败
            return null;
        }
        List<Catgory> catgoryList = roleDao.findAllCatgoryByUserId((int) user.getId());
        user.setCatgoryList(catgoryList);
        user.setLastLoginIp(getIpAddress(request));
        user.setLastLoginTime(System.currentTimeMillis()/1000);
        int loginCount = user.getSuccessLoginCount()+1;
        user.setSuccessLoginCount(loginCount);
        userDao.editUser(user);
        return user;
    }

    public static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    public static int getUserId(HttpServletRequest request) throws BizException{
        HttpSession session = request.getSession(true);
        User user = (User) session.getAttribute("user");


        if (user == null) {
            throw new BizException(CommonErrors.PERMISSION_DENIED);
        }
        return (int)user.getId();
    }
}
