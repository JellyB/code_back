package com.huatu.ztk.user.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.huatu.ztk.user.bean.UcenterBind;
import com.huatu.ztk.user.bean.UcenterMember;
import com.huatu.ztk.user.bean.UserDto;
import com.huatu.ztk.user.common.UcenterConfig;
import com.huatu.ztk.user.dao.UcenterDao;
import com.huatu.ztk.user.utils.UcenterUtils;

/**
 * ucenterService
 * Created by linkang on 7/11/16.
 */

@Service
public class UcenterService {
    private static final Logger logger = LoggerFactory.getLogger(UcenterService.class);


    @Autowired
    private UcenterDao ucenterDao;

    /**
     *ucenter保存用户信息
     * @param username
     * @param password
     * @param regip
     * @param appid
     * @return
     */
    public UcenterMember saveMember(String username, String password, String regip, int appId, boolean isEncypt) {
        String salt = UcenterUtils.salt_get(UcenterConfig.UC_PWD_UCMEMBERS_SALT_LENGTH_ZHUANTIKU);
        if(!isEncypt) {
        	//在线用户需要加密 教师网同步的已经加过密
        	password = UcenterUtils.password_encypt(password,salt,UcenterConfig.UCENTER_PASSWORD_ENCYPT_TYPE);
        }
        String email = username + "@ztk.com";
        int currentTime = (int)(System.currentTimeMillis() / 1000);
        UcenterMember ucenterMember = UcenterMember.builder()
                .username(username)
                .password(password)
                .email(email)
                .myid("")
                .myidkey("")
                .regip(regip)
                .regdate(currentTime)
                .lastloginip(0)
                .lastlogintime(currentTime)
                .salt(salt)
                .secques("")
                .appid(appId)
                .credit(0)
                .build();
        return ucenterDao.saveMember(ucenterMember);
    }

    /**
     * 修改密码
     * @param userDto
     * @param oldPassword
     * @param newPassword
     */
    public void updateMemberPwd(UserDto userDto, String oldPassword, String newPassword) {
        //修改密码，userDto一定不为空
        UcenterMember ucenterMember = findMemberByUsername(userDto.getName());
        if (ucenterMember != null) {
            boolean isCorret = checkUcenterPassword(ucenterMember, oldPassword);
            if (isCorret) {
                resetPwd(ucenterMember,newPassword);
            }
        } else {
            logger.error(">>> ucenterMember is null,ucenter modify pwd fail. username={},phone={}",
                    userDto.getName(),userDto.getMobile());
        }
    }


    /**
     * 完善个人信息，密码
     * @param userDto
     * @param newPassword
     * @return
     */
    public void setPwd(UserDto userDto, String mobile,String newPassword) {
        long t1 = System.currentTimeMillis();
        UcenterMember ucenterMember = findMemberByUsername(userDto.getName());
        logger.info("getUcenterMember expendTime={}", System.currentTimeMillis() - t1);
        if (ucenterMember != null) {
            long t2 = System.currentTimeMillis();
            resetPwd(ucenterMember, newPassword);
            logger.info("resetPwd expendTime={}", System.currentTimeMillis() - t2);
        } else {
            logger.error(">>> ucenterMember is null,ucenter reset pwd fail. username/phone={}",mobile);
        }
    }


    /**
     * ucenter重置密码
     * @param ucenterMember
     * @param newPassword
     * @return
     */
    public void resetPwd(UcenterMember ucenterMember,String newPassword) {
        String salt = UcenterUtils.salt_get(UcenterConfig.UC_PWD_UCMEMBERS_SALT_LENGTH_ZHUANTIKU);
        String md5Pwd = UcenterUtils.password_encypt(newPassword,salt,UcenterConfig.UCENTER_PASSWORD_ENCYPT_TYPE);
        ucenterDao.updateUserPwd(ucenterMember.getUsername(),md5Pwd,salt);
    }

    public UcenterMember findMemberByNameAndEmail(String username) {
       return ucenterDao.findMemberByNameAndEmail(username);
    }

    /**
     * 根据手机号查询绑定信息
     * @param mobile
     * @return
     */
    public UcenterBind findBind(String mobile) {
        return ucenterDao.findBind(mobile);
    }


    public List<UcenterBind> findBindByMobileList(List<String> mobileList){
        return ucenterDao.findBindByMobileList(mobileList);
    }

    /**
     * 根据用户名，手机号，email查询绑定信息
     * @param account
     * @return
     */
    public UcenterBind findAnyBind(String account) {
        return ucenterDao.findAnyBind(account);
    }

    /**
     * 根据username查找用户
     * @param username
     * @return
     */
    public UcenterMember findMemberByUsername(String username) {
        return ucenterDao.findMemberByUsername(username);
    }

    /**
     * uc绑定
     * @param userId
     * @param username
     * @param mobile
     */
    public void ucBind(int userId, String username, String mobile,String email) {
        UcenterBind ucenterBind = UcenterBind.builder()
                .userid(userId)
                .username(username)
                .email(email)
                .phone(mobile)
                .bd("1,0")
                .build();
        logger.info("ucenterBind = {}",ucenterBind);
        ucenterDao.ucbind(ucenterBind);
    }

    /**
     * ucenter密码验证，模仿userapi
     * @param ucenterMember
     * @param pwd
     * @return
     */
    public boolean checkUcenterPassword(UcenterMember ucenterMember, String pwd) {
        String md5Password = "";
        String md5Password2 = "";
        String ucpwd = ucenterMember.getPassword();
        String salt = ucenterMember.getSalt();

        // 根据数据库中加密密码的长度,区分出密码的加密者来自与网校还是其它
        if (ucpwd.matches("^\\w{32}$")) {
            //默认加密方式
            md5Password = UcenterUtils.password_encypt(pwd, salt,1);
        } else {
            //网校使用的加密方式，8位
            md5Password = UcenterUtils.password_encypt(pwd, salt,3);
        }
        if (ucenterMember.getAppid() == 3) {
            md5Password2 = UcenterUtils.password_encypt(pwd, salt, 2);
        }
        return md5Password.equals(ucpwd) ? true : md5Password2.equals(ucpwd);
    }

    /**
     * 测试
     * 删除测试用户
     */
    public void delUser(String phone) {
        ucenterDao.delUser(phone);
    }

    /**
     * 更新绑定手机号
     *
     * @param uname
     * @param mobile
     */
    public void updateMobile(String uname, String mobile) {
        ucenterDao.updateMobile(uname, mobile);
    }

    public List<Map<String, Object>> pageData(long beginTime, long endTime, int pageNum, int pageSize) {
        List<Map<String, Object>> maps = ucenterDao.pageData(beginTime, endTime, pageNum, pageSize);
        //组装手机号码
        List<Map<String, Object>> collect = maps.parallelStream()
                .map(data -> {
                    Object username = data.get("username");
                    if (null != username) {
                        String usernameStr = username.toString();
                        UcenterBind bind = findAnyBind(usernameStr);
                        data.put("phone", bind == null ? "" : bind.getPhone());
                        return data;
                    }
                    data.put("phone", "");
                    return data;
                })
                .collect(Collectors.toList());
        return collect;
    }

    public long countNum(long beginTime, long endTime) {
        return ucenterDao.countNum(beginTime, endTime);
    }
    
    public List<UcenterBind> findBindList(String mobile) {
        return ucenterDao.findBindList(mobile);
    }

	public List<Map<String, Object>> findCorrectBindList(int startuserid) {
		List<Map<String, Object>> findBindList = ucenterDao.findBindList(startuserid);

		return findBindList;
	}
}
