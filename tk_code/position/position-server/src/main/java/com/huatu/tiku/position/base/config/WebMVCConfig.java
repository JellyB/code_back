package com.huatu.tiku.position.base.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * WebMVC配置
 * 
 * @author Geek-S
 *
 */
@Configuration
public class WebMVCConfig extends WebMvcConfigurerAdapter {

	private WeiXinInterceptor weiXinInterceptor;

	@Autowired
	public WebMVCConfig(WeiXinInterceptor weiXinInterceptor) {
		this.weiXinInterceptor = weiXinInterceptor;
	}


	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**").allowedMethods("PUT","POST","GET","DELETE");
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(weiXinInterceptor).addPathPatterns("/**").excludePathPatterns("/weChat/**",
				"/dic/**",
				"/position/findPosition",
				"/position/positionInfo/**",
                "/msg/**",
                "/area/**");
	}
}