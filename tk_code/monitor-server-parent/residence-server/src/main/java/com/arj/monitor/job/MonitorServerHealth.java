package com.arj.monitor.job;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONObject;
import com.arj.monitor.service.impl.InformServiceImpl;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author zhangchong 检测essay
 */
@Slf4j
@Component
public class MonitorServerHealth {
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private RedisTemplate redisTemplate;

	@Resource(name = "informServiceImpl")
	private InformServiceImpl informServiceImpl;

	private static final int SUCCESS_CODE = 1000000;
	private static final String SUCCESS_CODE_KEY = "code";
	private static final String SUCCESS_CODE_DATA = "data";
	private static final String SUCCESS_CODE_STATUS = "status";
	private static final String RABBIT = "rabbit";
	private static final String ESSAYURL = "http://192.168.100.56:11122/e/_monitor/health";

	private String phone = "13716429964,18910645425,17611401891,18810636682";

	@Scheduled(cron = "0/15 * * * * *")
	public void scheduled() throws URISyntaxException {
		log.info("start check appindex data");
		JSONObject jsonObject = restTemplate.getForObject(ESSAYURL, JSONObject.class);
		if (!jsonObject.getString(SUCCESS_CODE_STATUS).equals("UP")) {
			informServiceImpl.informDingDing(jsonObject.toJSONString(), ESSAYURL, "");
			informServiceImpl.informSms(phone, jsonObject.toJSONString());
			log.error("error:{}", jsonObject.toJSONString());
		}
	}

}
