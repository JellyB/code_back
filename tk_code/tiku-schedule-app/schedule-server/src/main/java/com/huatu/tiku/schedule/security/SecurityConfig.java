package com.huatu.tiku.schedule.security;

import com.google.common.collect.Lists;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * @author jbzm
 * @date Create on 2018/3/7 15:58
 */
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
//    @Autowired
//    private UserService userService;
//    @Autowired
//    private AuthenticationProvider pauthenticationProvider;

    /**
     * 添加有效配置
     *
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry expressionInterceptUrlRegistry = http.csrf().disable().authorizeRequests();

        expressionInterceptUrlRegistry
                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
//                .antMatchers("/resources/**", "/signup", "/about").permitAll()
//                .antMatchers("/admin/**").hasRole("admin")
//                .antMatchers("/db/**").access("hasRole('admin') and hasRole('dba')")
                .anyRequest().permitAll()
                .and()
                .cors()
                .and()
//                .formLogin()
//                .loginPage("/security/toLogin")
//                .loginProcessingUrl("/security/login")
//                .successForwardUrl("/security/success/login")
//                .failureForwardUrl("/security/false")
//                .and()
//                .logout()
//                .logoutUrl("/security/logout")
//                .logoutSuccessUrl("/security/success/logout")
//                .and()
//                .exceptionHandling().accessDeniedPage("/security/denied")
//                .and()
//                .rememberMe()
//                .rememberMeCookieName("cookieName")
//                .rememberMeParameter("paramName")
//                .key("jbzm-Security")
//                .userDetailsService(userService)   //自定义用户
        ;
    }

    //


    /**
     * 配置放行路径
     *
     * @param web
     * @throws Exception
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        super.configure(web);
    }

    /**
     * 配置身份验证
     *
     * @param auth
     * @throws Exception
     */
//    @Override
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
////        auth.inMemoryAuthentication()
////                .withUser("admin").password("admin").roles("admin")
////                .and()
////                .withUser("dba").password("dba").roles("dba", "admin");
//        auth.authenticationProvider(pauthenticationProvider);
//    }
//
//
//    @Bean
//    public CustomDaoAuthenticationProvider authenticationProvider(@Autowired UserService userService) {
//        CustomDaoAuthenticationProvider authenticationProvider = new CustomDaoAuthenticationProvider(userService);
//        return authenticationProvider;
//    }


    /**
     * 定义cors跨域访问
     *
     * @return
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedOrigins(Lists.newArrayList("*"));
        configuration.setAllowedMethods(Arrays.asList(HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.DELETE.name(), HttpMethod.PUT.name()));
        configuration.setAllowedHeaders(Lists.newArrayList("*"));
        configuration.setExposedHeaders(Lists.newArrayList(HttpHeaders.SET_COOKIE));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
