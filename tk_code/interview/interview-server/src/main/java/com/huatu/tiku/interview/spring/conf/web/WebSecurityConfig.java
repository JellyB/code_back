package com.huatu.tiku.interview.spring.conf.web;

import com.google.common.collect.Lists;
import com.huatu.tiku.interview.constant.WebParamConsts;
import com.huatu.tiku.interview.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.rememberme.InMemoryTokenRepositoryImpl;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * @author hanchao
 * @date 2017/12/27 17:06
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${spring.application.name:unknown}")
    private String applicationName;
    @Autowired
    private AdminService adminService;
    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //前端开发完成，放开下面的注释
        http.csrf().disable()
                .authorizeRequests()
                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                .antMatchers("/end/**")
                .authenticated()
                .anyRequest()
                .permitAll()
                .and()
                .cors()
                .and()
                .formLogin()
                .loginPage("/auth/tologin")
                .loginProcessingUrl("/auth/login")
                .successForwardUrl("/auth/success?login")
                .failureForwardUrl("/auth/fail")
                .and()
                .logout()
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/auth/success?logout")
                .and()
                .rememberMe()
                .rememberMeCookieName(WebParamConsts.REMEMBER_ME_COOKIE)
                .rememberMeParameter(WebParamConsts.REMEMBER_ME_PARAM)
                .key(applicationName)
                .userDetailsService(adminService)
                .tokenRepository(new InMemoryTokenRepositoryImpl())
                .and()
                .exceptionHandling();
//                .accessDeniedPage("/auth/denied")
//                .and()
//                .addFilterBefore(new CaptchaFilter("/auth/login"), UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        //取消拦截静态资源
        web.ignoring().antMatchers("/","/**/*.html","/**/*.css","/**/*.js","/**/*.ico","/img/**","/fonts/**","/l10n/**","/**/*.woff")
                .antMatchers("/validcode/img","/echo/**","/download/**");
    }


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        auth.inMemoryAuthentication()
//                .withUser("admin").password("admin").roles("admin")
//                .and()
//                .withUser("guest").password("guest").roles("guest");
        auth.authenticationProvider(authenticationProvider);
    }

    /**
     * controller中使用
     * @return
     */
    public AuthenticationManager getAuthenticationManager(){
        try {
            return this.authenticationManager();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedOrigins(Lists.newArrayList("*"));
        configuration.setAllowedMethods(Arrays.asList(HttpMethod.OPTIONS.name(),HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.DELETE.name(), HttpMethod.PUT.name()));
        configuration.setAllowedHeaders(Lists.newArrayList("*"));
        configuration.setExposedHeaders(Lists.newArrayList(HttpHeaders.SET_COOKIE));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

//    @Bean
//    public PasswordEncoder passwordEncoder(){
//        return new BCryptPasswordEncoder();
//    }
//
//
//    @Bean
//    public CustomDaoAuthenticationProvider authenticationProvider(@Autowired UserDetailsService userDetailService){
//        CustomDaoAuthenticationProvider authenticationProvider = new CustomDaoAuthenticationProvider(userDetailService);
//        return authenticationProvider;
//    }
   @Bean
   public CustomDaoAuthenticationProvider authenticationProvider(@Autowired  AdminService adminService) {
        CustomDaoAuthenticationProvider authenticationProvider = new CustomDaoAuthenticationProvider(adminService);
        return authenticationProvider;
    }
}

