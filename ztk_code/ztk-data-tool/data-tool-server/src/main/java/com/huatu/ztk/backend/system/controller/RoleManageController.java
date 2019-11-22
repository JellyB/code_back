package com.huatu.ztk.backend.system.controller;

import com.huatu.ztk.backend.system.bean.RoleAction;
import com.huatu.ztk.backend.system.bean.RoleMenu;
import com.huatu.ztk.backend.system.service.RoleManageService;
import com.huatu.ztk.backend.user.bean.User;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Author: xuhuiqiang
 * Time: 2016-12-04  17:28 .
 */
@RestController
@RequestMapping(value = "/system", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class RoleManageController {

    private static final Logger logger = LoggerFactory.getLogger(RoleManageController.class);
    @Autowired
    private RoleManageService roleManageService;

    /**
     * 列出后台管理系统所有角色
     *
     * @return
     */
    @RequestMapping(value = "role/list", method = RequestMethod.GET)
    public Object roleList() throws BizException {
        return roleManageService.findAllRole();
    }

    /**
     * 根据用户id，返回角色信息
     * @param id
     *
     * @return
     */
    @RequestMapping(value = "role/info", method = RequestMethod.GET)
    public Object roleInfo(@RequestParam int id) throws BizException {
        return roleManageService.findRoleById(id);
    }


    /**
     * 根据查询类型、内容，获取角色列表
     *
     * @return
     */
    @RequestMapping(value = "role/listByType", method = RequestMethod.GET)
    public  Object roleListByType(@RequestParam(required = false)int type,
                                  @RequestParam(required = false)String content){
        return roleManageService.findRoleByType(type,content);
    }

    /**
     * 根据输入角色信息，修改角色
     * @param roleAction
     *
     * @return
     */
    @RequestMapping(value = "role/edit", method = RequestMethod.PUT)
    public void roleEdit(@RequestBody RoleAction roleAction,HttpServletRequest request) throws  BizException{
        final HttpSession session = request.getSession(true);
        User user = (User) session.getAttribute("user");
        String account = "";
        if(user!=null){
            account = user.getAccount();
            System.out.println(account);
        }
        roleAction.getRole().setUpdateer(account);
        roleManageService.editRole(roleAction);
    }


    /**
     * 根据输入角色信息，删除角色
     * @param id
     *
     * @return
     */
    @RequestMapping(value = "role/delete", method = RequestMethod.DELETE)
    public void roleEdit(@RequestParam int id,HttpServletRequest request) throws  BizException{
        final HttpSession session = request.getSession(true);
        User user = (User) session.getAttribute("user");
        String account = "";
        if(user!=null){
            account = user.getAccount();
            System.out.println(account);
        }
        roleManageService.deleteRole(id,account);
    }

    /**
     * 根据输入角色信息，新增角色
     * @param roleAction
     *
     * @return
     */
    @RequestMapping(value = "role/add", method = RequestMethod.PUT)
    public void roleAdd(@RequestBody RoleAction roleAction, HttpServletRequest request) throws  BizException{
        logger.info("role add json={}", JsonUtil.toJson(roleAction));
        final HttpSession session = request.getSession(true);
        User user = (User) session.getAttribute("user");
        String account = "";
        if(user!=null){
            account = user.getAccount();
            System.out.println(account);
        }
        roleAction.getRole().setCreater(account);
        roleManageService.addRole(roleAction);
    }

    /**
     * 获取所有菜单信息
     *
     * @return
     */
    @RequestMapping(value = "role/allmenu", method = RequestMethod.GET)
    public Object allMenu() throws  BizException{
        return roleManageService.allActionForAddRole();
    }
}
