//package com.huatu.tiku.position.base.config;
//
//import org.springframework.data.domain.AuditorAware;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//
///**
// * 新增更新时候，自动维护操作人
// *
// * @author Geek-S
// *
// */
//@Component
//public class UserAuditorAware implements AuditorAware<Long> {
//
//	@Override
//	public Long getCurrentAuditor() {
//		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//		if (authentication == null || !authentication.isAuthenticated()) {
//			return null;
//		}
//
//		if (authentication.getPrincipal() instanceof CustomUser) {
//			return ((CustomUser) authentication.getPrincipal()).getId();
//		}
//
//		return null;
//	}
//}