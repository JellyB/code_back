package com.huatu.ztk.backend.system.service;

import com.huatu.ztk.backend.system.bean.*;
import com.huatu.ztk.backend.system.dao.ActionManageDao;
import com.huatu.ztk.backend.system.dao.MenuManageDao;
import com.huatu.ztk.backend.system.dao.RoleManageDao;
import com.huatu.ztk.commons.JsonUtil;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2016-12-04  17:24 .
 */
@Service
public class RoleManageService {
    private static final Logger logger = LoggerFactory.getLogger(RoleManageService.class);

    @Autowired
    RoleManageDao roleManageDao;

    @Autowired
    MenuManageDao menuManageDao;

    @Autowired
    MenuManageService menuManageService;

    @Autowired
    ActionManageService actionManageService;

    @Autowired
    ActionManageDao actionManageDao;

    /**
     * 查询后台管理系统的所角色
     * @return
     */
    public Object findAllRole() {
        return roleManageDao.findAllRole();
    }

    /**
     * 根据角色id，返回角色信息
     * @param id
     *
     * @return
     */
    public Object findRoleById(int id) {
        List<Action> allActionList = actionManageDao.findAllActionValid();//查找所有功能项
        List<Action> actionList = actionManageDao.findActionsByRoleId(id);//设置一个空actionList
        List<Action> lastActionList = new ArrayList<>();
        lastActionList.addAll(actionManageService.actionLogic(actionList,allActionList));
        RoleMessage roleMessage = roleManageDao.findRoleById(id);
        List<Catgory> allCatgoryList = roleManageDao.findAllCatgory();
        List<Catgory> catgoryList = roleManageDao.findAllCatgoryByRoleId(id);
        List<Catgory> lastCatgoryList = catgoryLogic(catgoryList,allCatgoryList);
        int lookup = 1;
        if(CollectionUtils.isNotEmpty((catgoryList))){
            lookup = catgoryList.get(0).getLookup();
        }
        logger.info("返回的roleMessage={}",roleMessage);
        RoleAction roleAction = RoleAction.builder()
                .role(roleMessage)
                .actionList(lastActionList)
                .catgoryList(lastCatgoryList)
                .lookup(lookup)
                .build();
        return roleAction;
    }

    /**
     * 根据查询类型、内容，返回角色列表
     * @param type、content
     *
     * @return
     */
    public Object findRoleByType(int type,String content) {
        List<RoleMessage> roleMessageList = new ArrayList<>();
        if(type==1){
            return roleManageDao.findRoleByName(content);
        }
        return roleMessageList;
    }

    /**
     * 根据角色id，删除该角色，即将角色置为无效
     * @param id
     *
     * @return
     */
    public void deleteRole(int id,String updaterName) {
        //roleManageDao.deleteRoleMenu(id);
        //roleManageDao.deleteRoleById(id);
        roleManageDao.toInvalidRole(id,updaterName);
    }

    /**
     * 根据输入角色信息，对角色进行修改
     * @param roleAction
     *
     * @return
     */
    public void editRole(RoleAction roleAction) {
        RoleMessage role = roleAction.getRole();
        List<Catgory> catgoryList = roleAction.getCatgoryList();
        int lookup = roleAction.getLookup();
        roleManageDao.editRole(role);
        roleManageDao.deleteRoleAction(role.getId());
        insertRoleAction(role.getId(),roleAction.getActionList());
        roleManageDao.deleteRoleCatgory(role.getId());
        insertRoleCatgory(role.getId(),catgoryList,lookup);
    }

    /**
     * 根据输入角色信息，新增角色
     * @param roleAction
     *
     * @return
     */
    public void addRole(RoleAction roleAction) {
        RoleMessage role = roleAction.getRole();
        roleManageDao.addRole(role);
        int insertRoleId = roleManageDao.findRoleByName(role.getName()).get(0).getId();
        List<Catgory> catgoryList = roleAction.getCatgoryList();
        int lookup = roleAction.getLookup();
        insertRoleAction(insertRoleId,roleAction.getActionList());
        insertRoleCatgory(insertRoleId,catgoryList,lookup);
    }

    /**
     * 返回所有功能（以树结构的形式）及一个空角色
     *
     * @return
     */
    public Object allActionForAddRole() {

        RoleMessage roleMessage = new RoleMessage();
        List<Action> allActionList = actionManageDao.findAllActionValid();//查找所有功能项
        List<Action> actionList = new ArrayList<>();//设置一个空actionList
        List<Action> lastActionList = new ArrayList<>();
        lastActionList.addAll(actionManageService.actionLogic(actionList,allActionList));
        List<Catgory> catgoryList = roleManageDao.findAllCatgory();
        int lookup = 1;
        if(CollectionUtils.isNotEmpty((catgoryList))){
            lookup = catgoryList.get(0).getLookup();
        }
        RoleAction roleAction = RoleAction.builder()
                .role(roleMessage)
                .actionList(lastActionList)
                .catgoryList(catgoryList)
                .lookup(lookup)
                .build();
        return roleAction;
    }


    /**
     * 根据角色id、功能列表，功能列，插入【角色、功能】对
     * @return
     */
    public void insertRoleAction(int roleid,List<Action> actionList){
        List<Integer> actionIds = new ArrayList<>();
        for(Action action:actionList){
            //若角色子功能不为空，判断该角色是否有该功能的权限
            int temp = 0;//用于标记，若角色拥有功能的子功能权限，那么角色也有该功能权限
            if(action.getSubAction()!=null||action.getSubAction().size()!=0){
                for(Action sa:action.getSubAction()){
                    if(sa.getIsBelong()==1){
                        int said = sa.getId();
                        //roleManageDao.addRoleAction(roleid,said);
                        actionIds.add(said);
                        temp = 1;
                        action.setIsBelong(1);//用于标记，若角色拥有功能的子功能权限，那么角色也有该功能权限
                    }
                }
            }else{
                action.setIsBelong(0);//若角色对于功能的所有子功能都没有权限，那么角色也没有该功能权限
            }
            //若该角色对该功能有权限，插入该功能
            if(action.getIsBelong()==1&&temp==1){
                int actionId = action.getId();
                actionIds.add(actionId);
                //roleManageDao.addRoleAction(roleid,actionId);
            }
        }
        roleManageDao.addRoleActions(roleid,actionIds);
    }

    /**
     * 根据角色id、功能列表，功能列，插入【角色、功能】对
     * @return
     */
    public void insertRoleCatgory(int roleid,List<Catgory> catgoryList,int lookup){
        List<Integer> catgoryIds = new ArrayList<>();
        for(Catgory catgory:catgoryList){
            //若该角色对该考试类型有权限，插入该功能
            if(catgory.getIsBelong()==1){
                int catgoryId = catgory.getId();
                catgoryIds.add(catgoryId);
            }
        }
        roleManageDao.addRoleCatgorys(roleid,catgoryIds,lookup);
    }

    public List<Catgory> catgoryLogic(List<Catgory> catgoryList,List<Catgory> allCatgoryList){
        for(int i=0;i<allCatgoryList.size();i++){
            Catgory catgory = allCatgoryList.get(i);
            if(isContain(catgory,catgoryList)){
                catgory.setIsBelong(1);
            }else {
                catgory.setIsBelong(0);
            }
            allCatgoryList.set(i,catgory);
        }
        return allCatgoryList;
    }

    public boolean isContain(Catgory catgory,List<Catgory> catgoryList){
        boolean result = false;
        int id = catgory.getId();
        for(Catgory ca:catgoryList){
            if(id==ca.getId()){
                result = true;
                break;
            }
        }
        return result;
    }
}
