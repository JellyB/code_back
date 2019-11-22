package com.huatu.springboot.dubbo.health;

import com.alibaba.dubbo.config.spring.ReferenceBean;
import com.alibaba.dubbo.rpc.service.EchoService;
import org.springframework.beans.BeansException;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;
import java.util.Objects;

/**
 * dubbo healthindicator
 * @author hanchao
 * @date 2017/5/6 1:38
 */
public class DubboHealthIndicator extends AbstractHealthIndicator implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private static final String ECHO_MESSAGE = "OK";

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        builder.up();
        Map<String, ReferenceBean> references = applicationContext.getBeansOfType(ReferenceBean.class);
        references.forEach( (beanName,referenceBean) -> {
            String referenceName = referenceBean.getInterface();
            String key = beanName+" -> "+referenceName;
            EchoService echoService = ((EchoService)referenceBean.get());
            try {
                Object message =  echoService.$echo(ECHO_MESSAGE);
                if(Objects.equals(ECHO_MESSAGE,message)){
                    builder.withDetail(key,ECHO_MESSAGE);
                }else{
                    builder.down().withDetail(key,"回声测试失败,响应："+String.valueOf(message));
                }
            } catch(Exception e){
                builder.down().withDetail(key,"回声测试异常："+e.getMessage());
            }
        });
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
