package com.huatu.ztk.backend.system.controller;

import com.huatu.ztk.backend.system.bean.UserRole;
import com.huatu.ztk.backend.system.service.UserManageService;
import com.huatu.ztk.backend.user.bean.User;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.itextpdf.text.BadElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Author: xuhuiqiang
 * Time: 2016-11-23  15:38 .
 */
@RestController
@RequestMapping(value = "/system", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class UserManageController {

    private static final Logger logger = LoggerFactory.getLogger(UserManageController.class);

    @Autowired
    private UserManageService userManageService;


    /**
     * 列出后台管理系统所有用户
     *
     * @return
     */
    @RequestMapping(value = "user/list", method = RequestMethod.GET)
    public Object userList() throws BizException {
        return userManageService.findAllUser();
    }


    /**
     * 列出后台管理系统所有角色
     *
     * @return
     */
    @RequestMapping(value = "user/allRole", method = RequestMethod.GET)
    public Object findAllRole() throws BizException {
        return userManageService.findAllRole();
    }

    /**
     * 根据用户ID返回后台管理系统的用户信息
     * @param id
     * @return
     */
    @RequestMapping(value = "user/info", method = RequestMethod.GET)
    public Object userInfo(@RequestParam int id) throws BizException {
        return userManageService.getUser(id);
    }


    /**
     * 修改用户信息
     * @param ue
     * @return
     */
    @RequestMapping(value = "user/edit", method = RequestMethod.PUT)
    public void userEdit(@RequestBody UserRole ue) throws BizException {
        userManageService.editUser(ue);
    }
    /**
     * 删除系统用户
     *
     * @return
     */
    @RequestMapping(value = "user/delete", method = RequestMethod.DELETE)
    public boolean userDelete(@RequestParam int id) throws BizException {
        return userManageService.deleteUser(id);
    }

    /**
     * 新增用户信息
     * @param ur
     * @return
     */
    @RequestMapping(value = "user/add", method = RequestMethod.POST)
    public void userAdd(@RequestBody UserRole ur,HttpServletRequest request) throws BizException {
        final HttpSession session = request.getSession(true);
        User user = (User) session.getAttribute("user");
        String account = "";
        int uid = 2;
        if(user!=null){
            account = user.getAccount();
            uid =(int) user.getId();
        }
        userManageService.addUser(ur,account,uid);
    }


    /**
     * 根据查询类型、内容，获取菜单列表
     *
     * @return
     */
    @RequestMapping(value = "user/listByType", method = RequestMethod.GET)
    public  Object userListByType(int type,String content){
        return userManageService.findUserByType(type,content);
    }

}
