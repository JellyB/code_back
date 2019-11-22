package com.huatu.ztk.backend.system.service;

import com.huatu.ztk.backend.system.bean.*;
import com.huatu.ztk.backend.system.controller.RoleManageController;
import com.huatu.ztk.backend.system.dao.ActionManageDao;
import com.huatu.ztk.backend.system.dao.MenuManageDao;
import com.huatu.ztk.backend.system.dao.OperateDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2017-04-25  14:40 .
 */
@Service
public class OperateService {
    private static final Logger logger = LoggerFactory.getLogger(OperateService.class);
    @Autowired
    OperateDao operateDao;
    @Autowired
    ActionManageDao actionManageDao;
    @Autowired
    ActionManageService actionManageService;

    /**
     * 查询所有操作
     * @return
     */
    public Object findAllOperate(){
        return operateDao.findAllOperate();
    }

    /**
     * 根据查询类型、内容，获取操作列表
     * @return
     */
    public Object findOperateByType(int type,String content) {
        if(type==1){
            return operateDao.findOperateByName(content);
        }else if(type==2){
            return operateDao.findOperateByUrl(content);
        }else{
            return operateDao.findOperateByActionName(content);
        }
    }

    /**
     * 根据id，返回operate和所有功能信息
     * @param id
     * @return
     */
    public OperateAction findOmById(int id){
        List<Action> allActionList = actionManageDao.findAllActionValid();//查找所有功能项
        List<Action> actionList = new ArrayList<>();//设置一个空actionList
        List<Action> lastActionList = new ArrayList<>();
        lastActionList.addAll(actionManageService.actionLogic(actionList,allActionList));
        OperateMessage operate = operateDao.findOperateById(id);
        OperateAction operateAction = OperateAction.builder()
                .operate(operate)
                .actionList(lastActionList)
                .build();
        return operateAction;
    }

    /**
     * 获得所有功能信息
     */
    public List<Action> findAllAction(){
        List<Action> allActionList = actionManageDao.findAllActionValid();//查找所有功能项
        List<Action> actionList = new ArrayList<>();//设置一个空actionList
        List<Action> lastActionList = new ArrayList<>();
        lastActionList.addAll(actionManageService.actionLogic(actionList,allActionList));
        return lastActionList;
    }

    /**
     * 根据operate，新增操作
     * @param operate
     */
    public void addOperate(OperateMessage operate){
        operateDao.addOperate(operate);
    }

    /**
     * 根据operate，修改操作
     * @param operate
     */
    public void editOperate(OperateMessage operate){
        operateDao.editOperate(operate);
    }

    /**
     * 根据id，删除操作
     */
    public void deleteOperate(int id){
        operateDao.deleteOperate(id);
    }
}
