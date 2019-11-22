package com.huatu.splider.spring.conf.web;

import com.huatu.splider.SpliderApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

/**
 * @author hanchao
 * @date 2017/10/24 10:18
 */
@Configuration
@ServletComponentScan(basePackageClasses = SpliderApplication.class)//servlet扫描配置
public class WebMvcConfig extends WebMvcConfigurerAdapter {
    /**
     * 为了方便定义消息处理器的顺序，理论上处理器越少，处理中，需要循环遍历判断的次数也越少
     */
    @Autowired
    private MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter;
    @Autowired
    private StringHttpMessageConverter stringHttpMessageConverter;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        super.addArgumentResolvers(argumentResolvers);
    }

    /**
     * springBoot中的messageconverters构建-》httpmessageconverters->webmvcconfig(configures)->autowebmvcConfig
     * httpmessageconverters会影响feign中使用到的converter，所以从这里修改
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
}
