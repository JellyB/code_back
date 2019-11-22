package com.huatu.morphling.task;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.utils.collection.HashMapBuilder;
import com.huatu.morphling.dao.jpa.entity.Env;
import com.huatu.morphling.service.local.EnvService;
import com.huatu.morphling.service.local.UserService;
import com.huatu.tiku.common.consts.RabbitConsts;
import com.rabbitmq.client.*;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 定时健康检查的任务
 * @author hanchao
 * @date 2018/1/16 18:28
 */
@Slf4j
@Component
public class DeadLetterResendTask {
    @Autowired
    private UserService userService;
    @Autowired
    private EnvService envService;
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    @Autowired
    @Qualifier("coreThreadPool")
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;


    @Scheduled(cron = "0 0 3 * * ?")
    public void execute() {
        List items = Lists.newArrayList();
        for (Env env : envService.getEnvs().values()) {
            Map item = Maps.newHashMap();
            item.put("env",env.getName());
            int ackCount = 0;
            int hitCount = 0;
            try {
                Env.RabbitMqConfig rabbitMq = env.getProperties().getRabbitMq();
                if(rabbitMq == null){
                    continue;
                }
                String addresses = rabbitMq.getAddress();
                item.put("rabbit",addresses);
                List<Address> addressList = Arrays.stream(addresses.split(",")).map(address -> {
                    String[] arr = address.split(":");
                    return new Address(arr[0], Integer.parseInt(arr[1]));
                }).collect(Collectors.toList());
                ConnectionFactory connectionFactory = new ConnectionFactory();
                connectionFactory.setUsername(rabbitMq.getUsername());
                connectionFactory.setPassword(rabbitMq.getPassword());
                connectionFactory.setVirtualHost(rabbitMq.getVirtualhost());
                connectionFactory.setConnectionTimeout(10000);

                Connection connection = connectionFactory.newConnection(addressList);
                Channel channel = connection.createChannel();

                while(true){
                    GetResponse response = channel.basicGet(RabbitConsts.DLQ_DEFAULT, false);
                    if(response == null){
                        break;
                    }
                    hitCount++;
                    try {
                        List deathHeaders = (List) response.getProps().getHeaders().get("x-death");
                        Map deathHeader = (Map) deathHeaders.get(0);
                        List routingKeys = (List) deathHeader.get("routing-keys");
                        for (Object routingKey : routingKeys) {
                            channel.basicPublish(String.valueOf(deathHeader.get("exchange")),String.valueOf(routingKey),response.getProps(),response.getBody());
                        }
                        channel.basicAck(response.getEnvelope().getDeliveryTag(),false);
                        ackCount++;
                    } catch(Exception e){
                        e.printStackTrace();
                    }
                }


                channel.close();
                connection.close();
            } catch(Exception e){
                e.printStackTrace();
            }
            item.put("ackCount",ackCount);
            item.put("hitCount",hitCount);
            items.add(item);
        }

        Set<String> to = userService.findByRole(1).stream()
                .map(u -> u.getEmail())
                .filter(email -> StringUtils.isNotBlank(email))
                .collect(Collectors.toSet());



        MimeMessage message = mailSender.createMimeMessage();
        try {
            Template template = freeMarkerConfigurer.getConfiguration().getTemplate("/mail/deadletter_report.ftl");
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, HashMapBuilder.newBuilder().put("items",items).build());

            MimeMessageHelper helper = new MimeMessageHelper(message,true,"utf-8");
            helper.setFrom(from);
            helper.setTo(to.stream().toArray(String[]::new));
            helper.setSubject("死信队列重发报告");
            helper.setText(content,true);
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
