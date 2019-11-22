package com.huatu.ztk.backend.system.service;

import com.huatu.ztk.backend.system.bean.RoleMessage;
import com.huatu.ztk.backend.system.bean.UserRole;
import com.huatu.ztk.backend.system.bean.UserMessage;
import com.huatu.ztk.backend.system.dao.RoleManageDao;
import com.huatu.ztk.backend.system.dao.UserManageDao;
import com.huatu.ztk.commons.exception.BizException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2016-11-23  15:44 .
 */
@Service
public class UserManageService {
    private static final Logger logger = LoggerFactory.getLogger(UserManageService.class);

    @Autowired
    private UserManageDao userManageDao;
    @Autowired
    private RoleManageDao roleManageDao;

    /**
     * 查询后台管理系统的所有用户
     * @return
     */
    public Object findAllUser() {
        return userManageDao.findAllUser();
    }
    /**
     * 根据用户ID返回后台管理系统的用户信息
     * @return
     */
    public Object getUser(int id){
        List<RoleMessage> roles = roleManageDao.findRoleByUserId(id);
        List<RoleMessage> allroles = roleManageDao.findAllRoleValid();
        UserMessage userMessage = userManageDao.findUserById(id);
        UserRole userRole = UserRole.builder()
                .userMessage(userMessage)
                .allRoles(isSelected(roles,allroles))
                .build();
        return userRole;
    }
    /**
     *  获取所有角色信息（添加一个空userMessage信息）
     * @return
     */
    public Object findAllRole(){
        List<RoleMessage> allroles = roleManageDao.findAllRoleValid();
        UserMessage userMessage = new UserMessage();
        UserRole userRole = UserRole.builder()
                .userMessage(userMessage)
                .allRoles(allroles)
                .build();
        return userRole;
    }
    /**
     * 修改用户信息
     * @return
     */
    public void editUser(UserRole ue){
        UserMessage userMessage = ue.getUserMessage();
        List<RoleMessage> allRoles = ue.getAllRoles();
        userManageDao.editUserStatus(userMessage.getId(),userMessage.getStatus());
        userManageDao.deleteUserRole(userMessage.getId());
        allRoles.stream().filter(role -> role.getIsbelong() == 1).forEach(role -> {
            userManageDao.insertUserRole(userMessage.getId(), role.getId());
        });
        //若密码修改了，需重新进行md5加密
        if(userMessage.getPassword().length()>0){
            String password = userMessage.getPassword();
            final String tmp = Hex.encodeHexString((DigestUtils.getMd5Digest().digest(password.getBytes())));
            String slat = tmp.substring(tmp.length()-6,tmp.length()-6+4);
            final String shaSlat = Hex.encodeHexString(DigestUtils.getSha256Digest().digest(slat.getBytes()));
            final String finalPassword = Hex.encodeHexString(DigestUtils.getMd5Digest().digest((shaSlat +password).getBytes()));
            userManageDao.editUserPassword(userMessage.getId(),finalPassword);
        }

    }
    /**
     * 删除后台管理系统用户
     * @return
     */
    public boolean deleteUser(int id) {
        return userManageDao.deleteUser(id);
    }
    /**
     * 新增后台管理系统用户
     * @return
     */
    public void addUser(UserRole ur,String account,int uid) throws BizException {
        UserMessage userMessage = ur.getUserMessage();
        logger.info("userMessage={}",userMessage);
        List<RoleMessage> allRoles = ur.getAllRoles();
        String password = userMessage.getPassword();
        final String tmp = Hex.encodeHexString((DigestUtils.getMd5Digest().digest(password.getBytes())));
        String slat = tmp.substring(tmp.length()-6,tmp.length()-6+4);
        final String shaSlat = Hex.encodeHexString(DigestUtils.getSha256Digest().digest(slat.getBytes()));
        final String finalPassword = Hex.encodeHexString(DigestUtils.getMd5Digest().digest((shaSlat +password).getBytes()));
        userMessage.setPassword(finalPassword);
        userMessage.setCreator(account);
        userMessage.setCreatorId(uid);
        int id = userManageDao.addUser(userMessage);
        logger.info("返回的id={}",id);
        allRoles.stream().filter(role -> role.getIsbelong() == 1).forEach(role -> {
            userManageDao.insertUserRole(id, role.getId());
        });
    }

    /**
     * 根据查询类型、内容，返回用户信息
     * @param type， content
     * @return
     */
    public Object findUserByType(int type,String content) {
        if(type==1)
            return userManageDao.findUserByName(content);
        else
            return userManageDao.findUserByAccount(content);

    }


    //针对每个用户有权限的角色列表roles，角色全列表allroles中进行标记
    public List<RoleMessage> isSelected(List<RoleMessage> roles,List<RoleMessage> allroles){
        List<RoleMessage> rfuelist = new ArrayList<>();
        for(RoleMessage allrole:allroles){
            int id = allrole.getId();
            String name = allrole.getName();
            int isbelong = 0;
            for(RoleMessage role:roles){
                if(id==role.getId()){
                    isbelong = 1;
                    break;
                }
            }
            RoleMessage rfue = RoleMessage.builder()
                    .id(id)
                    .name(name)
                    .isbelong(isbelong)
                    .build();
            rfuelist.add(rfue);
        }
        return rfuelist;
    }
}
