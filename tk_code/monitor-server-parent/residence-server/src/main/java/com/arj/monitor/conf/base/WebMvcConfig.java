package com.arj.monitor.conf.base;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

/**
 * @author zhouwei
 */
@Configuration
public class WebMvcConfig extends   WebMvcConfigurerAdapter {

    /**
     * 为了方便定义消息处理器的顺序，理论上处理器越少，处理中，需要循环遍历判断的次数也越少
     */
    @Autowired
    private MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter;
    @Autowired
    private StringHttpMessageConverter stringHttpMessageConverter;

    /**
     * springBoot中的messageconverters构建-》httpmessageconverters->webmvcconfig(configures)->autowebmvcConfig
     * httpmessageconverters会影响feign中使用到的converter，所以从这里修改
     *
     * @param converters
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.clear();
        //jackson放在第一位，因为在默认没有声明accept以及handler produce的时候，会根据处理器去判断
        converters.add(mappingJackson2HttpMessageConverter);
        converters.add(stringHttpMessageConverter);
        converters.add(new ByteArrayHttpMessageConverter());
    }


    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedMethods("PUT", "DELETE","POST","GET")
        ;
    }

}
