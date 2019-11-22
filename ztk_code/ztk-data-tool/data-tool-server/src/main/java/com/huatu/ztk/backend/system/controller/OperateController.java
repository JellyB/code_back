package com.huatu.ztk.backend.system.controller;

import com.huatu.ztk.backend.system.bean.Operate;
import com.huatu.ztk.backend.system.bean.OperateAction;
import com.huatu.ztk.backend.system.bean.OperateMenu;
import com.huatu.ztk.backend.system.bean.OperateMessage;
import com.huatu.ztk.backend.system.service.OperateService;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * Author: xuhuiqiang
 * Time: 2017-04-25  14:38 .
 */
@RestController
@RequestMapping(value = "/system", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class OperateController {
    private static final Logger logger = LoggerFactory.getLogger(OperateController.class);
    @Autowired
    private OperateService operateService;

    /**
     * 列出操作项
     * @return
     */
    @RequestMapping(value = "operateManage/list", method = RequestMethod.GET)
    public Object operateList() throws BizException {
        return operateService.findAllOperate();
    }

    /**
     * 根据查询类型，内容，返回操作列表
     * @param type,content
     */
    @RequestMapping(value = "operateManage/listByType", method = RequestMethod.GET)
    public  Object operateListByType(@RequestParam(required = false)int type, @RequestParam(required = false)String content){
        return operateService.findOperateByType(type,content);
    }

    /**
     * 根据id，删除操作
     */
    @RequestMapping(value = "operateManage/delete",method = RequestMethod.DELETE)
    public void operateDelete(@RequestParam(required = false)int id){
        operateService.deleteOperate(id);
    }

    /**
     * 根据id，获取operate及所有功能信息
     * @param id
     */
    @RequestMapping(value = "operateManage/omById",method = RequestMethod.GET)
    public Object omById(@RequestParam(required = false)int id){
        return operateService.findOmById(id);
    }

    /**
     * 根据operateMenu，修改操作
     * @param operateAction
     */
    @RequestMapping(value = "operateManage/edit",method = RequestMethod.PUT)
    public void operateEdit(@RequestBody(required = false) OperateAction operateAction){
        operateService.editOperate(operateAction.getOperate());
    }

    /**
     * 获取所有功能信息
     */
    @RequestMapping(value = "operateManage/allAction",method = RequestMethod.GET)
    public Object actionAll(){
        return operateService.findAllAction();
    }

    /**
     * 根据operate，新增操作
     * @param operate
     */
    @RequestMapping(value = "operateManage/add",method = RequestMethod.POST)
    public void operateAdd(@RequestBody(required = false)OperateMessage operate){
        operateService.addOperate(operate);
    }
}
