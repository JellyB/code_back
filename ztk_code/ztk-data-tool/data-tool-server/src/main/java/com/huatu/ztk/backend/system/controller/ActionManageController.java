package com.huatu.ztk.backend.system.controller;

import com.huatu.ztk.backend.system.bean.Action;
import com.huatu.ztk.backend.system.bean.OperateMenu;
import com.huatu.ztk.backend.system.service.ActionManageService;
import com.huatu.ztk.backend.system.service.OperateManageService;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * Author: xuhuiqiang
 * Time: 2017-04-22  19:27 .
 */
@RestController
@RequestMapping(value = "/system", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class ActionManageController {
    private static final Logger logger = LoggerFactory.getLogger(OperateManageController.class);
    @Autowired
    private ActionManageService actionManageService;

    /**
     * 列出功能项
     * @return
     */
    @RequestMapping(value = "action/list", method = RequestMethod.GET)
    public Object actionList() throws BizException {
        return actionManageService.findAllAction();
    }

    /**
     * 列出顶级功能项
     * @return
     */
    @RequestMapping(value = "action/allTopAction", method = RequestMethod.GET)
    public Object actionTopList() throws BizException {
        return actionManageService.findAllTopAction();
    }

    /**
     * 根据查询类型，内容，返回功能列表
     * @param type,content
     */
    @RequestMapping(value = "action/listByType", method = RequestMethod.GET)
    public  Object actionListByType(@RequestParam(required = false)int type, @RequestParam(required = false)String content){
        return actionManageService.findActionByType(type,content);
    }

    /**
     * 根据id，删除功能
     */
    @RequestMapping(value = "action/delete",method = RequestMethod.DELETE)
    public void actionDelete(@RequestParam(required = false)int id){
        actionManageService.deleteAction(id);
    }

    /**
     * 根据action，新增操作
     * @param action
     */
    @RequestMapping(value = "action/add",method = RequestMethod.POST)
    public void actionAdd(@RequestBody Action action){
        logger.info("operate add json={}", JsonUtil.toJson(action));
        actionManageService.addAction(action);
    }

    /**
     * 根据id，获取action及所有顶级功能菜单
     * @param id
     */
    @RequestMapping(value = "action/amById",method = RequestMethod.GET)
    public Object amById(@RequestParam(required = false)int id){
        return actionManageService.findAmById(id);
    }

    /**
     * 根据action，修改功能
     * @param action
     */
    @RequestMapping(value = "action/edit",method = RequestMethod.PUT)
    public void actionEdit(@RequestBody Action action){
        actionManageService.editAction(action);
    }
}
