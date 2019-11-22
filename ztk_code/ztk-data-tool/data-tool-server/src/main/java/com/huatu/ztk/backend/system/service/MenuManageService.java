package com.huatu.ztk.backend.system.service;


import com.google.common.collect.Lists;
import com.huatu.ztk.backend.system.bean.Menu;
import com.huatu.ztk.backend.system.common.error.MenuError;
import com.huatu.ztk.backend.system.dao.MenuManageDao;
import com.huatu.ztk.backend.system.dao.RoleManageDao;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Author: xuhuiqiang
 * Time: 2016-12-04  13:21 .
 */
@Service
public class MenuManageService {
    private static final Logger logger = LoggerFactory.getLogger(MenuManageService.class);

    @Autowired
    MenuManageDao menuManageDao;
    @Autowired
    RoleManageDao roleManageDao;

    /**
     * 查询后台管理系统的所有菜单项
     * @return
     */
    public Object findAllMenu() {
        return menuManageDao.findAllMenu();
    }

    /**
     * 根据查询类型、内容，获取菜单列表
     * @return
     */
    public Object findMenuByType(int type,String content) {
        if(type==1){
            return menuManageDao.findMenuByName(content);
        }else{
            return menuManageDao.findSubmenuByName(content);
        }

    }


    /**
     * 根据用户name，获取菜单树结构
     * @return
     */
    public Object findMenuByUname(String uname) {

        List<Menu> menuList = menuManageDao.findMenuByUname(uname);
        List<Menu> nullList = new ArrayList<>();
        menuList = menuLogic(nullList,menuList);
        return menuList;
    }


    /**
     * 根据用户id，获取菜单树结构
     * @return
     */
    public Object findMenuByUid(int uid) {
        long start = System.currentTimeMillis();
//        List<Menu> realMenus = menuManageDao.findMenuByUid(uid);
        List<Menu> menuList = menuManageDao.findAllMenu();
        List<String> templateUrls  =  menuManageDao.findTemplateUrlByUid(uid);
        menuList.removeIf(i->i.getStatus()!=1);
        if(CollectionUtils.isEmpty(menuList)){
            return menuList;
        }
        List<Menu> secondList = Lists.newArrayList();
        List<Menu> firstList = Lists.newArrayList();
        List<Menu> realMenus = Lists.newArrayList();
        menuList.stream().forEach(i->{if(i.getParentId()==0){firstList.add(i);}else{secondList.add(i);}});
        realMenus.addAll(secondList.parallelStream().filter(i->templateUrls.contains(i.getTemplateUrl())).collect(Collectors.toList()));
        List<Integer> parents = realMenus.stream().map(i->i.getParentId()).collect(Collectors.toList());
        realMenus.addAll(firstList.parallelStream().filter(i->parents.contains(i.getId())).collect(Collectors.toList()));
        long end = System.currentTimeMillis();
        logger.info("find meun use time {}",end-start);
        List<Menu> nullList = new ArrayList<>();
        return menuLogic(nullList,realMenus);
    }

    /**
     * 根据菜单id，删除菜单；
     * @param id
     * @return
     */
    public void deleteMenu(int id) throws BizException {
        if(menuManageDao.findSubmenuById(id).size()==0){
            try {
                menuManageDao.deleteRoleMenu(id);
                menuManageDao.deleteMenu(id);
            }catch (Exception e){
                e.printStackTrace();
                throw new BizException(MenuError.Delete_FAIL);
            }
        }else{
            throw new BizException(MenuError.Delete_HAVASUBMENU);
        }

    }


    /**
     * 根据菜单id返回菜单信息及所有的顶级菜单信息
     * @param id
     * @return
     */
    public Object getMenu(int id){
        Menu menu = menuManageDao.findMenuById(id);
        List<Menu> topMenuList = menuManageDao.findAllTopMenu();
        Menu topMenu = Menu.builder()
                .id(0)
                .parentId(0)
                .text("自身为顶级菜单")
                .build();
        topMenuList.add(topMenu);
        menu.setSubMenu(topMenuList);//topMenuList不是menu的子菜单链表，因数据结构相同，用该数据结构
        return menu;
    }


    /**
     * 返回所有的顶级菜单信息及一个空menu
     * @param
     * @return
     */
    public Object getTopMenu(){
        Menu menu = new Menu();
        List<Menu> topMenuList = menuManageDao.findAllTopMenu();
        Menu topMenu = Menu.builder()
                .id(0)
                .parentId(0)
                .text("自身为顶级菜单")
                .build();
        topMenuList.add(topMenu);
        menu.setSubMenu(topMenuList);//topMenuList不是menu的子菜单链表，因数据结构相同，用该数据结构
        return menu;
    }

    /**
     * 根据菜单字符串，修改菜单信息
     * @param str
     * @return
     */
    public void editMenu(String str,String updaterName) throws BizException {

        Map<String,Object> result = JsonUtil.toMap(str);

        int parentId = (int) result.get("parentId");
        Menu menu = JsonUtil.toObject(JsonUtil.toJson(result.get("menu")),Menu.class);

        if(parentId==0&&menu.getParentId()!=0&&!menu.getSref().equals("#")){
            throw new BizException(MenuError.EDIT_PARENT);
        }else if(parentId==0&&menu.getParentId()!=0&&menu.getSref().equals("#")){
            throw new BizException(MenuError.EDIT_PARENTMOVE);
        }else if(parentId==0&&menu.getParentId()==0&&!menu.getSref().equals("#")){
            throw new BizException(MenuError.EDIT_PARENTSREF);
        }else{

            menu.setUpdateer(updaterName);
            if(menu.getParentId()==0){
                menu.setParentName("自身为顶级菜单");
            }else{
                menu.setParentName(menuManageDao.findMenuById(menu.getParentId()).getText());
            }
            menuManageDao.editMenu(menu);
        }
        //System.out.println("我是结束标志！");
    }

    /**
     * 新增菜单
     *
     * @param menu
     * @return
     */
    public void addMenu(Menu menu,String createrName) throws BizException {

        int parentId = menu.getParentId();
        String sref = menu.getSref();
        String parentName = "";
        menu.setIsbelong(1);//标记超级管理员有该菜单的权限
        menu.setCreater(createrName);

       // System.out.println("我们也是测试的："+parentId+"   menu2:"+sref);
        if(parentId==0&&!sref.equals("#")){
            throw new BizException(MenuError.ADD_PARENTSREF);
        }else{
            if(parentId==0)
                parentName = "自身为顶级菜单";
            else{
                parentName = menuManageDao.findMenuById(parentId).getText();
            }
            menu.setParentName(parentName);
            menuManageDao.addMenu(menu);
        }
        //System.out.println("我是结束标志！");
    }



    /**
     * 输出allMenuList树结构，并判断菜单是否有menuList中，若在isbelong设置为1
     * @return
     */

    public List<Menu> menuLogic(List<Menu> menuList,List<Menu> allMenuList){
        List<Menu> lastMenuList = new ArrayList<Menu>();
        for(Menu menu:allMenuList){
            int parentId = menu.getParentId();
            int id = menu.getId();
            if(isContain(menu,menuList))
                menu.setIsbelong(1);
            int sign = 0;//用于标记该menu是否已经存在于lastMenuList中，若为1，表示存在，否则不存在
            for(int i=0;i<lastMenuList.size();i++){
                Menu lastmenu = lastMenuList.get(i);
                //System.out.println("lastmenu: "+lastmenu);
                //若上级菜单已经存在于lastMenuList则直接加在该上级菜单的子菜单链表中
                if(parentId==lastmenu.getId()){
                    if(lastmenu.getSubMenu()==null){

                        List<Menu> subMenuList = new ArrayList<>();
                        subMenuList.add(menu);
                        lastmenu.setSubMenu(subMenuList);
                    }else{
                        lastmenu.getSubMenu().add(menu);
                    }

                    lastMenuList.set(i,lastmenu);
                    sign = 1;
                    break;
                }
                //若menu为顶级菜单，且顶级菜单ID存在于lastMenuList中，则直接替换lastMenuList中lastmenu顶级菜单
                if (parentId==0&&lastmenu.getId()==id){
                    lastmenu.setId(id);
                    lastmenu.setText(menu.getText());
                    lastmenu.setSref(menu.getSref());
                    lastmenu.setIsbelong(menu.getIsbelong());
                    lastMenuList.set(i,lastmenu);
                    sign = 1;
                    break;
                }
            }

            //没有在lastMenuList中发现该menu
            if(sign==0){
                if(parentId==0){
                    lastMenuList.add(menu);
                }else{
                    List<Menu> subMenuList = new ArrayList<>();
                    subMenuList.add(menu);
                    Menu parentmenu = Menu.builder()
                            .id(menu.getParentId())
                            .subMenu(subMenuList)
                            .build();
                    lastMenuList.add(parentmenu);
                }
            }
        }
        return lastMenuList;
    }


    public boolean isContain(Menu menu,List<Menu> menuList){
        boolean result = false;
        int id = menu.getId();
        for(Menu mu:menuList){
            if(id==mu.getId()){
                result = true;
                break;
            }
        }
        return result;
    }

}
