package com.huatu.tiku.schedule.biz.service.imple;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.huatu.tiku.schedule.base.service.impl.BaseServiceImpl;
import com.huatu.tiku.schedule.biz.domain.Menu;
import com.huatu.tiku.schedule.biz.repository.MenuRepository;
import com.huatu.tiku.schedule.biz.service.MenuService;

@Service
public class MenuServiceImpl extends BaseServiceImpl<Menu, Long> implements MenuService {

    private final MenuRepository menuRepository;

    @Autowired
    public MenuServiceImpl(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    @Override
    public Set<Menu> getMenus(Long teacherId) {
        return menuRepository.getMenus(teacherId);
    }

}
