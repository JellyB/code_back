package com.huatu.tiku.schedule.biz.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.Query;

import com.huatu.tiku.schedule.base.repository.BaseRepository;
import com.huatu.tiku.schedule.biz.domain.Menu;

/**
 * 菜单Repository
 *
 * @author Geek-S
 */
public interface MenuRepository extends BaseRepository<Menu, Long> {

	/**
	 * 获取当前用户的菜单
	 * 
	 * @return 菜单列表
	 */
	@Query("select distinct menu from Menu menu join menu.roles role join role.teachers teacher where teacher.id = ?1")
	Set<Menu> getMenus(Long teacherId);
}
