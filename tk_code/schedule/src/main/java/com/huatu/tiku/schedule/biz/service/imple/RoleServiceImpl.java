package com.huatu.tiku.schedule.biz.service.imple;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.huatu.tiku.schedule.base.service.impl.BaseServiceImpl;
import com.huatu.tiku.schedule.biz.domain.Role;
import com.huatu.tiku.schedule.biz.repository.RoleRepository;
import com.huatu.tiku.schedule.biz.service.RoleService;

@Service
public class RoleServiceImpl extends BaseServiceImpl<Role, Long> implements RoleService {

	private final RoleRepository roleRepository;

	public RoleServiceImpl(RoleRepository roleRepository) {
		this.roleRepository = roleRepository;
	}

	@Override
	public Set<Role> findByTeachersId(Long teacherId) {
		return roleRepository.findByTeachersId(teacherId);
	}

}
