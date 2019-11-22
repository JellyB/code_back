package com.huatu.ztk.backend.system.service;

import com.huatu.ztk.backend.system.bean.Menu;
import com.huatu.ztk.backend.system.bean.Operate;
import com.huatu.ztk.backend.system.bean.OperateMenu;
import com.huatu.ztk.backend.system.dao.MenuManageDao;
import com.huatu.ztk.backend.system.dao.OperateManageDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2017-01-16  17:02 .
 */
@Service
public class OperateManageService {
    @Autowired
    OperateManageDao operateManageDao;
    @Autowired
    MenuManageDao menuManageDao;
    @Autowired
    MenuManageService menuManageService;

    /**
     * 查询所有操作
     * @return
     */
    public Object findAllOperate(){
        return operateManageDao.findAllOperate();
    }

    /**
     * 根据查询类型、内容，获取操作列表
     * @return
     */
    public Object findOperateByType(int type,String content) {
        if(type==1){
            return operateManageDao.findOperateByName(content);
        }else if(type==2){
            return operateManageDao.findOperateByUrl(content);
        }else{
            return operateManageDao.findOperateByNMenuName(content);
        }
    }

    /**
     * 根据id，返回operate和所有菜单信息
     * @param id
     * @return
     */
    public OperateMenu findOmById(int id){
        List<Menu> allMenuList = menuManageDao.findAllMenu();//查找所有菜单项
        List<Menu> menuList = new ArrayList<>();//设置一个空menuList
        List<Menu> lastMenuList = new ArrayList<>();
        lastMenuList.addAll(menuManageService.menuLogic(menuList,allMenuList));
        Operate operate = operateManageDao.findOperateById(id);
        OperateMenu operateMenu = OperateMenu.builder()
                .operate(operate)
                .menus(lastMenuList)
                .build();
        return operateMenu;
    }

    /**
     * 获得所有菜单信息
     */
    public List<Menu> findAllMenu(){
        List<Menu> allMenuList = menuManageDao.findAllMenu();//查找所有菜单项
        List<Menu> menuList = new ArrayList<>();//设置一个空menuList
        List<Menu> lastMenuList = new ArrayList<>();
        lastMenuList.addAll(menuManageService.menuLogic(menuList,allMenuList));
        return lastMenuList;
    }

    /**
     * 根据operate，新增操作
     * @param operate
     */
    public void addOperate(Operate operate){
        operateManageDao.addOperate(operate);
    }

    /**
     * 根据operate，修改操作
     * @param operate
     */
    public void editOperate(Operate operate){
        operateManageDao.editOperate(operate);
    }

    /**
     * 根据id，删除操作
     */
    public void deleteOperate(int id){
        operateManageDao.deleteOperate(id);
    }
}
