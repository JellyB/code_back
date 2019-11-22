package com.huatu.tiku.search.test;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.huatu.ztk.question.api.QuestionDubboService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author hanchao
 * @date 2017/4/18 18:07
 */
public class GlobalDubboTest {
    private RegistryConfig registry;
    private ApplicationConfig application;
    @Before
    @Ignore
    public void test(){
        // 当前应用配置
        application = new ApplicationConfig();
        application.setName("test");

        // 连接注册中心配置
        registry = new RegistryConfig();
        registry.setAddress("zookeeper://192.168.100.21:2181");

    }

    @Test
    @Ignore
    public void testService(){
        ReferenceConfig<QuestionDubboService> reference = new ReferenceConfig<QuestionDubboService>(); // 此实例很重，封装了与注册中心的连接以及与提供者的连接，请自行缓存，否则可能造成内存和连接泄漏
        reference.setApplication(application);
        reference.setVersion("2.2");
        reference.setRegistry(registry); // 多个注册中心可以用setRegistries()
        reference.setInterface(QuestionDubboService.class);
        System.out.println(JSON.toJSONString(reference.get().findById(261110), SerializerFeature.PrettyFormat));
    }
}
