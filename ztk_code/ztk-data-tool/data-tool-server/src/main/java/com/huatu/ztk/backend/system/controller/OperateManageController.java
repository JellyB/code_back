package com.huatu.ztk.backend.system.controller;


import com.alibaba.dubbo.common.json.Jackson;
import com.huatu.ztk.backend.system.bean.Operate;
import com.huatu.ztk.backend.system.bean.OperateMenu;
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
 * Time: 2017-01-16  17:01 .
 */
@RestController
@RequestMapping(value = "/system", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class OperateManageController {
    private static final Logger logger = LoggerFactory.getLogger(OperateManageController.class);
    @Autowired
    private OperateManageService operateManageService;

    /**
     * 列出操作项
     * @return
     */
    @RequestMapping(value = "operate/list", method = RequestMethod.GET)
    public Object operateList() throws BizException {
        return operateManageService.findAllOperate();
    }

    /**
     * 根据查询类型，内容，返回操作列表
     * @param type,content
     */
    @RequestMapping(value = "operate/listByType", method = RequestMethod.GET)
    public  Object operateListByType(@RequestParam(required = false)int type, @RequestParam(required = false)String content){
        return operateManageService.findOperateByType(type,content);
    }

    /**
     * 根据id，删除操作
     */
    @RequestMapping(value = "operate/delete",method = RequestMethod.DELETE)
    public void operateDelete(@RequestParam(required = false)int id){
        operateManageService.deleteOperate(id);
    }

    /**
     * 根据id，获取operate及所有菜单信息
     * @param id
     */
    @RequestMapping(value = "operate/omById",method = RequestMethod.GET)
    public Object omById(@RequestParam(required = false)int id){
        return operateManageService.findOmById(id);
    }

    /**
     * 根据operateMenu，修改操作
     * @param operateMenu
     */
    @RequestMapping(value = "operate/edit",method = RequestMethod.PUT)
    public void operateEdit(@RequestBody(required = false) OperateMenu operateMenu){
        operateManageService.editOperate(operateMenu.getOperate());
    }

    /**
     * 获取所有菜单信息
     */
    @RequestMapping(value = "operate/allMenu",method = RequestMethod.GET)
    public Object menuAll(){
        return operateManageService.findAllMenu();
    }

    /**
     * 根据operate，新增操作
     * @param operate
     */
    @RequestMapping(value = "operate/add",method = RequestMethod.POST)
    public void operateAdd(@RequestBody(required = false)Operate operate){
        operateManageService.addOperate(operate);
    }
}
