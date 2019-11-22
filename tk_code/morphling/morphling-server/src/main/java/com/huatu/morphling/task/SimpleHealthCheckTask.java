package com.huatu.morphling.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.utils.date.TimestampUtil;
import com.huatu.morphling.common.bean.AppInstanceVO;
import com.huatu.morphling.common.enums.InstanceStatus;
import com.huatu.morphling.dao.jpa.entity.App;
import com.huatu.morphling.service.local.AppService;
import com.huatu.morphling.service.local.EnvService;
import com.huatu.morphling.service.local.UserAppService;
import com.huatu.morphling.service.remote.endpoint.HealthEndpointService;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 定时健康检查的任务
 * @author hanchao
 * @date 2018/1/16 18:28
 */
@Slf4j
@Component
public class SimpleHealthCheckTask {
    private static final Cache<String,Object> warnLock = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    @Autowired
    private HealthEndpointService healthEndpointService;
    @Autowired
    private AppService appService;
    @Autowired
    private EnvService envService;
    @Autowired
    private UserAppService userAppService;
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;


    @Scheduled(cron = "0/30 * * * * ?")
    public void check() throws IOException {
        envService.getEnvs().values().stream().filter(e -> e.isProd()).forEach(env -> {
            List<App> apps = appService.findByEnv(env.getKey());
            for (App app : apps) {
                new CheckHealthTask(app).run();
            }
        });
    }



    private class CheckHealthTask implements Runnable {
        private App app;

        public CheckHealthTask(App app) {
            this.app = app;
        }

        @Override
        public void run() {
            List<AppInstanceVO> instances = appService.findInstances(app.getId());
            List<Object> errors = Lists.newArrayList();
            for (AppInstanceVO instance : instances) {
                String errorInfo = null;
                try {
                    Map healthInfo = healthEndpointService.request(instance.getHost(), instance.getPort(), instance.getContextPath());
                    if( ! HealthEndpointService.UP.equalsIgnoreCase(String.valueOf(healthInfo.get("status")))){
                        errorInfo = JSON.toJSONString(healthInfo, SerializerFeature.PrettyFormat);
                    }
                } catch(Exception e){
                    if(instance.getStatus() == InstanceStatus.RUNNING.getCode()
                            && instance.getUpdateTime() != null
                            && (TimestampUtil.currentUnixTimeStamp() - (instance.getUpdateTime().getTime()/1000)) > 300){//启动超过5分钟
                        errorInfo = ExceptionUtils.getStackTrace(e);
                    }
                }

                String key = app.getName()+"$"+instance.getHost()+"$"+instance.getPort();

                if(errorInfo != null && warnLock.asMap().putIfAbsent(key,true) == null){ //五分钟内未报警
                    Map error = Maps.newHashMap();
                    error.put("host",instance.getHost());
                    error.put("port",instance.getPort());
                    error.put("info",errorInfo);
                    errors.add(error);
                }
            }
            if(errors.size() > 0){
                Map data = Maps.newHashMap();
                data.put("app",app);
                data.put("errors",errors);


                //查询所有负责项目的用户
                Set<String> to = userAppService.findByApp(app.getId()).stream()
                        .map(u -> u.getEmail())
                        .filter(email -> StringUtils.isNotBlank(email))
                        .collect(Collectors.toSet());

                if(CollectionUtils.isEmpty(to)){
                    log.error("cant find any address to send mail...");
                    return;
                }

                MimeMessage message = mailSender.createMimeMessage();
                try {
                    Template template = freeMarkerConfigurer.getConfiguration().getTemplate("/mail/health_warn.ftl");
                    String content = FreeMarkerTemplateUtils.processTemplateIntoString(template,data);

                    MimeMessageHelper helper = new MimeMessageHelper(message,true,"utf-8");
                    helper.setFrom(from);
                    helper.setTo(to.stream().toArray(String[]::new));
                    helper.setSubject(app.getName()+" 健康检查报警");
                    helper.setText(content,true);
                    mailSender.send(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
