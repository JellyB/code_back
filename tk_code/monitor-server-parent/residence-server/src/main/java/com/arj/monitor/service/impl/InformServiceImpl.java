package com.arj.monitor.service.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.arj.monitor.common.CommonConfig;
import com.arj.monitor.entity.InformRecord;
import com.arj.monitor.entity.ServerInfo;
import com.arj.monitor.repository.InformRecordRepository;
import com.arj.monitor.util.MDSmsUtil;
import com.dingtalk.chatbot.message.ActionCardAction;
import com.dingtalk.chatbot.message.ActionCardMessage;
import com.google.common.base.Splitter;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhouwei
 * @Description: TODO
 * @create 2019-01-07 下午7:49
 **/
@Slf4j
@Component("informServiceImpl")
public class InformServiceImpl {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private InformRecordRepository informRecordRepository;

    public void informDingDing(String content, String url,String ip) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        //String textMsg = "{ \"msgtype\": \"text\", \"text\": {\"content\": \""+"地址: "+url+" 报警: "+content.replaceAll("\"", "") +"\"}}";
        log.info("send dingding msg:{}",content);
        ActionCardMessage message = new ActionCardMessage();
        message.setTitle("报警信息");
        message.setBannerURL("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1547720784337&di=f940e86f644afffa91d73674b43b74a0&imgtype=0&src=http%3A%2F%2Fpic.58pic.com%2F58pic%2F14%2F44%2F13%2F80z58PICk5V_1024.png");
        message.setBriefTitle("中间件报警信息");
        message.setBriefText(content);
        ActionCardAction action1 = new ActionCardAction("详情", url.replace("localhost", ip));
        message.addAction(action1);


        //String textMsg = "{ \"msgtype\": \"link\", \"link\": {\"text\":\" " + content + "\",\"title\": \"报警了\",  \"picUrl\": \"\",  \"messageUrl\": \"" + url + "\"}}";
        HttpEntity<String> r = new HttpEntity<>(message.toJsonString(), headers);

        String data = restTemplate.postForObject(CommonConfig.WEBHOOK_TOKEN, r, String.class);
        log.info("################" + data);
    }

    public void informSms(String telephone, String content) {
        if (StringUtils.isNotBlank(telephone)) {
            if (telephone.contains(",")) {
                List<String> phoneList = Splitter.on(",").trimResults().splitToList(telephone);
                phoneList.forEach(p -> {
                    MDSmsUtil.sendMessage(p, content.replaceAll(",", "，").replaceAll(":", "：") + "服务异常");
                });
            } else {
                MDSmsUtil.sendMessage(telephone, content.replaceAll(",", "，").replaceAll(":", "：") + "服务异常");
            }

        }
    }
    public void inform(ServerInfo serverInfo, String content, long minute) {

        switch (serverInfo.getAlarmType()) {
            case 0: {
                informDingDing(content, serverInfo.getUrl(),serverInfo.getIp());
                break;
            }
            case 1: {
                informSms(serverInfo.getTelephone(), content);
                break;
            }
            default:
                break;
        }
        //生成一条警情，没处理就会一直报警
       // saveInformRecord(serverInfo, content, minute);
    }

    @Async
   public void saveInformRecord(ServerInfo serverInfo, String reason, long minute) {
        InformRecord informRecord = new InformRecord();
        informRecord.setYear(Integer.parseInt(DateFormatUtils.format(new Date(), "yyyy")));
        informRecord.setMonth(Integer.parseInt(DateFormatUtils.format(new Date(), "yyyyMM")));
        informRecord.setDay(Integer.parseInt(DateFormatUtils.format(new Date(), "yyyyMMdd")));
        informRecord.setHour(Integer.parseInt(DateFormatUtils.format(new Date(), "yyyyMMddHH")));
        informRecord.setMinute(minute);
        informRecord.setSecond(Long.parseLong(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss")));
        informRecord.setServerInfoId(serverInfo.getId());
        informRecord.setReason(reason);
        informRecord.setAlarmType(serverInfo.getAlarmType());
        informRecord.setUrl(serverInfo.getUrl());
        informRecordRepository.save(informRecord);
    }
}
