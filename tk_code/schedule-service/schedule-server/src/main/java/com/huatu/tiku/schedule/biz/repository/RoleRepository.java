package com.huatu.tiku.schedule.biz.repository;

import java.util.Set;

import com.huatu.tiku.schedule.base.repository.BaseRepository;
import com.huatu.tiku.schedule.biz.domain.Role;

/**
 * 角色Repository
 *
 * @author Geek-S
 */
public interface RoleRepository extends BaseRepository<Role, Long> {

	/**
	 * 根据教师ID获取角色
	 * 
	 * @param teacherId
	 *            教师ID
	 * @return 角色列表
	 */
	Set<Role> findByTeachersId(Long teacherId);
}
