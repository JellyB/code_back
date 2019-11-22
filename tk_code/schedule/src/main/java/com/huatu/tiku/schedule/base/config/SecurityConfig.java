package com.huatu.tiku.schedule.base.config;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.huatu.tiku.schedule.base.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import com.google.common.collect.Lists;
import com.huatu.tiku.schedule.biz.domain.Role;
import com.huatu.tiku.schedule.biz.domain.Teacher;
import com.huatu.tiku.schedule.biz.enums.ExamType;
import com.huatu.tiku.schedule.biz.enums.TeacherStatus;
import com.huatu.tiku.schedule.biz.service.RoleService;
import com.huatu.tiku.schedule.biz.service.TeacherService;

/**
 * Security配置
 * 
 * @author Geek-S
 *
 */
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	/**
	 * 密码加密算法
	 * 
	 * @return PasswordEncoder
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	private final TeacherService teacherService;

	private final RoleService roleService;

	@Autowired
	public SecurityConfig(TeacherService teacherService, RoleService roleService) {
		this.teacherService = teacherService;
		this.roleService = roleService;
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.cors();

		http.csrf().disable();

		// 集成权限后开启
		http.authorizeRequests().anyRequest().authenticated();

		http.formLogin().failureHandler(new AuthenticationFailureHandler() {

			@Override
			public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
					AuthenticationException exception) throws IOException, ServletException {
				response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
                if(exception instanceof DisabledException){
                    response.getWriter().print("{\"status\": 200,\"success\": false,\"message\": \"用户状态异常\"}");
                    return ;

                }
                if(exception instanceof InternalAuthenticationServiceException){
                    response.getWriter().print("{\"status\": 200,\"success\": false,\"message\": \"登录失败,没有此账户\"}");
                    return ;

                }
                response.getWriter().print("{\"status\": 200,\"success\": false,\"message\": \"密码错误\"}");
			}
		}).successHandler(new AuthenticationSuccessHandler() {

			@Override
			public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
					Authentication authentication) throws IOException, ServletException {
				response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);

				response.getWriter().print("{\"status\": 200,\"success\": true,\"message\": \"登录成功\"}");
			}
		});

		http.logout().logoutSuccessHandler(new LogoutSuccessHandler() {

			@Override
			public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
					Authentication authentication) throws IOException, ServletException {
				response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);

				response.getWriter().print("{\"status\": 200,\"success\": true,\"message\": \"登出成功\"}");
			}
		});

		http.exceptionHandling().authenticationEntryPoint(new AuthenticationEntryPoint() {

			@Override
			public void commence(HttpServletRequest request, HttpServletResponse response,
					AuthenticationException authException) throws IOException, ServletException {
				response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);

				response.getWriter().print("{\"status\": 401,\"message\": \"未登录\"}");
			}
		});
	}

	@Override
	@Bean
	public UserDetailsService userDetailsService() {
		return new UserDetailsService() {

			@Override
			public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

				Teacher user = teacherService.findByPhone(username);

				if (user == null) {
					// 用户不存在
					throw new BadRequestException("UsernameNotFound");
				} else {
					List<GrantedAuthority> grantedAuthorities = Lists.newArrayList();

					// 获取用户权限
					Set<String> authorities = teacherService.getAuthorities(user.getId());

					authorities.forEach(authority -> {
						grantedAuthorities.add(new SimpleGrantedAuthority(authority));
					});

					CustomUser customUser = new CustomUser(user.getId(), user.getPhone(), user.getPassword(),
							TeacherStatus.ZC.equals(user.getStatus()), grantedAuthorities);
					customUser.setName(user.getName());
					customUser.setExamType(user.getExamType());
					customUser.setSubjectId(user.getSubjectId());
					customUser.setLeaderFlag(user.getLeaderFlag());
					customUser.setTeacherType(user.getTeacherType());
					customUser.setPhone(user.getPhone());

					Set<Role> roles = roleService.findByTeachersId(user.getId());
					customUser.setRoles(roles);

					Set<ExamType> examTypes = teacherService.findDataPermissionIdsById(user.getId());
					customUser.setDataPermissions(examTypes);

					return customUser;
				}
			}
		};
	}

}