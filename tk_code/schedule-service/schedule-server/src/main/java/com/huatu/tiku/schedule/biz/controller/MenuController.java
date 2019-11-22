package com.huatu.tiku.schedule.biz.controller;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.schedule.base.config.CustomUser;
import com.huatu.tiku.schedule.biz.domain.Menu;
import com.huatu.tiku.schedule.biz.service.MenuService;
import com.huatu.tiku.schedule.biz.vo.LeftMenuVo;
import com.huatu.tiku.schedule.biz.vo.LeftMenuVo.SubMenu;

/**
 * 菜单Controller
 *
 * @author Geek-S
 */
@RestController
@RequestMapping("menu")
public class MenuController {

    private final MenuService menuService;

    @Autowired
    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    /**
     * 获取当前用户的权限菜单
     *
     * @param user 当前用户
     * @return 左侧菜单
     */
    @GetMapping
    public List<LeftMenuVo> list(@AuthenticationPrincipal CustomUser user) {
        Set<Menu> menus = menuService.getMenus(user.getId());

        // 拼装菜单
        List<LeftMenuVo> leftMenuVos = Lists.newArrayList();

        // 一级菜单
        Map<Long, LeftMenuVo> parentMenus = Maps.newHashMap();

        Iterator<Menu> menuIterator = menus.iterator();
        while (menuIterator.hasNext()) {
            Menu temp = menuIterator.next();

            if (temp.getParentId() == null) {
                LeftMenuVo leftMenuVo = new LeftMenuVo();
                leftMenuVo.setTitle(temp.getName());
                leftMenuVo.setList(Lists.newArrayList());

                leftMenuVos.add(leftMenuVo);
                parentMenus.put(temp.getId(), leftMenuVo);

                menuIterator.remove();
            }
        }

        menus.forEach(menu -> {
            if (menu.getParentId() != null) {
                SubMenu subMenu = new SubMenu();
                subMenu.setBt(menu.getName());
                subMenu.setLink(menu.getRoute());

                parentMenus.get(menu.getParentId()).getList().add(subMenu);
            }
        });

        return leftMenuVos;
    }

}
