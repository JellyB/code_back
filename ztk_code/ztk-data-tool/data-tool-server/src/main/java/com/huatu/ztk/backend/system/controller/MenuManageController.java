package com.huatu.ztk.backend.system.controller;

import com.huatu.ztk.backend.system.bean.Menu;
import com.huatu.ztk.backend.system.service.MenuManageService;
import com.huatu.ztk.backend.user.bean.User;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.mongodb.util.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

/**
 * Author: xuhuiqiang
 * Time: 2016-11-23  15:39 .
 */
@RestController
@RequestMapping(value = "/system", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class MenuManageController {
    private static final Logger logger = LoggerFactory.getLogger(MenuManageController.class);
    @Autowired
    private MenuManageService menuManageService;

    /**
     * 列出后台管理系统所有菜单项
     *
     * @return
     */
    @RequestMapping(value = "menu/list", method = RequestMethod.GET)
    public Object menuList() throws BizException {
        return menuManageService.findAllMenu();
    }


    /**
     * 根据查询类型、内容，获取菜单列表
     *
     * @return
     */
    @RequestMapping(value = "menu/listByType", method = RequestMethod.GET)
    public  Object menuListByType(@RequestParam(required = false)int type,
                                  @RequestParam(required = false)String content){
        return menuManageService.findMenuByType(type,content);
    }

    /**
     * 根据菜单id，删除菜单
     *@param id
     * @return
     */
    @RequestMapping(value = "menu/delete", method = RequestMethod.GET)
    public void menuDelete(@RequestParam int id) throws BizException {
        menuManageService.deleteMenu(id);
    }

    /**
     * 根据session获取用户uname，获得用户有权限的菜单列表
     *@param
     * @return
     */

    @RequestMapping(value = "menu/sidebar", method = RequestMethod.GET)
    public Object userMenuList(HttpServletRequest request)throws BizException {
        final HttpSession session = request.getSession(true);
        User user = (User) session.getAttribute("user");
        String account = "";
        int id = 0;
        if(user!=null){
            account = user.getAccount();
            id = (int) user.getId();
        }
        logger.info("account={},id={}",account,id);
        return menuManageService.findMenuByUid(id);
    }

    /**
     *获得菜单信息
     *@param id
     * @return
     */
    @RequestMapping(value = "menu/info", method = RequestMethod.GET)
    public Object menuLInfo(@RequestParam int id) throws BizException {
        return menuManageService.getMenu(id);
    }

    /**
     *获得所有顶级菜单
     * @return
     */
    @RequestMapping(value = "menu/alltop", method = RequestMethod.GET)
    public Object menuLInfo() throws BizException {
        return menuManageService.getTopMenu();
    }

    /**
     * 修改菜单
     * @return
     */
    @RequestMapping(value = "menu/edit", method = RequestMethod.PUT)
    public void menuDelete(@RequestBody String str,HttpServletRequest request) throws BizException {

        final HttpSession session = request.getSession(true);
        User user = (User) session.getAttribute("user");
        String account = "";
        if(user!=null){
            account = user.getAccount();
        }
        menuManageService.editMenu(str,account);
    }


    /**
     * 新增菜单
     * @return
     */
    @RequestMapping(value = "menu/add", method = RequestMethod.POST)
    public void menuAdd(@RequestBody Menu menu,HttpServletRequest request) throws BizException {
        logger.info("menu edit json={}", JsonUtil.toJson(menu));

        final HttpSession session = request.getSession(true);
        User user = (User) session.getAttribute("user");
        String account = "";
        if(user!=null){
            account = user.getAccount();
            System.out.println(account);
        }
        menuManageService.addMenu(menu,account);
    }

}
