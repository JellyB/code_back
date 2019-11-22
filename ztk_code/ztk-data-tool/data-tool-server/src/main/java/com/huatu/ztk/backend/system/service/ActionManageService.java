package com.huatu.ztk.backend.system.service;

import com.huatu.ztk.backend.question.service.QuestionService;
import com.huatu.ztk.backend.system.bean.Action;
import com.huatu.ztk.backend.system.dao.ActionManageDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2017-04-22  19:29 .
 */
@Service
public class ActionManageService {
    private static final Logger logger = LoggerFactory.getLogger(QuestionService.class);
    @Autowired
    private ActionManageDao actionManageDao;

    /**
     * 查询所有功能
     * @return
     */
    public Object findAllAction(){
        return actionManageDao.findAllAction();
    }

    /**
     * 查询所有顶级功能
     * @return
     */
    public Object findAllTopAction(){
        List<Action> actionList = actionManageDao.findAllTopAction();
        Action action = Action.builder()
                .id(0)
                .name("自身为顶级功能")
                .build();
        actionList.add(action);
        return actionList;
    }

    /**
     * 根据查询类型、内容，获取功能列表
     * @return
     */
    public Object findActionByType(int type,String content) {
        return actionManageDao.findActionByType(type,content);
    }

    /**
     * 根据id，删除功能
     */
    public void deleteAction(int id){
        actionManageDao.deleteAction(id);
    }

    /**
     * 根据action，新增操作
     * @param action
     */
    public void addAction(Action action){
        actionManageDao.addAction(action);
    }

    /**
     * 根据id，返回action和所有顶级功能信息
     * @param id
     * @return
     */
    public Object findAmById(int id){
        List<Action> allAction = new ArrayList<>();
        List<Action> allTopAction = actionManageDao.findAllTopAction();
        Action actionFirst = Action.builder()
                .id(0)
                .name("自身为顶级功能")
                .build();
        allTopAction.add(actionFirst);
        Action action = actionManageDao.findActionById(id);
        allAction.add(action);
        allAction.addAll(allTopAction);
        return allAction;
    }

    /**
     * 根据action，修改功能
     * @param action
     */
    public void editAction(Action action){
        actionManageDao.editAction(action);
    }

    /**
     * 输出allActionList树结构，并判断菜单是否有ActionList中，若在isbelong设置为1
     * @return
     */

    public List<Action> actionLogic(List<Action> ActionList,List<Action> allActionList){
        List<Action> lastActionList = new ArrayList<Action>();
        for(Action Action:allActionList){
            int parentId = Action.getParentId();
            int id = Action.getId();
            if(isContain(Action,ActionList))
                Action.setIsBelong(1);
            int sign = 0;//用于标记该Action是否已经存在于lastActionList中，若为1，表示存在，否则不存在
            for(int i=0;i<lastActionList.size();i++){
                Action lastAction = lastActionList.get(i);
                //若上级菜单已经存在于lastActionList则直接加在该上级菜单的子菜单链表中
                if(parentId==lastAction.getId()){
                    if(lastAction.getSubAction()==null){

                        List<Action> subActionList = new ArrayList<>();
                        subActionList.add(Action);
                        lastAction.setSubAction(subActionList);
                    }else{
                        lastAction.getSubAction().add(Action);
                    }

                    lastActionList.set(i,lastAction);
                    sign = 1;
                    break;
                }
                //若Action为顶级菜单，且顶级菜单ID存在于lastActionList中，则直接替换lastActionList中lastAction顶级菜单
                if (parentId==0&&lastAction.getId()==id){
                    lastAction.setId(id);
                    lastAction.setName(Action.getName());
                    lastAction.setIsBelong(Action.getIsBelong());
                    lastActionList.set(i,lastAction);
                    sign = 1;
                    break;
                }
            }

            //没有在lastActionList中发现该Action
            if(sign==0){
                if(parentId==0){
                    lastActionList.add(Action);
                }else{
                    List<Action> subActionList = new ArrayList<>();
                    subActionList.add(Action);
                    Action parentAction = Action.builder()
                            .id(Action.getParentId())
                            .subAction(subActionList)
                            .build();
                    lastActionList.add(parentAction);
                }
            }
        }
        return lastActionList;
    }


    public boolean isContain(Action Action,List<Action> ActionList){
        boolean result = false;
        int id = Action.getId();
        for(Action mu:ActionList){
            if(id==mu.getId()){
                result = true;
                break;
            }
        }
        return result;
    }
}
