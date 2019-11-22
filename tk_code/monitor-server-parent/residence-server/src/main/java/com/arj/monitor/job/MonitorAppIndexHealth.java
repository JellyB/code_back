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
 * @author zhangchong 检测app首页知识点监控情况
 */
@Slf4j
@Component
public class MonitorAppIndexHealth {
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private RedisTemplate redisTemplate;
	@Resource(name = "informServiceImpl")
	private InformServiceImpl informServiceImpl;

	private static final int SUCCESS_CODE = 1000000;
	private static final String SUCCESS_CODE_KEY = "code";
	private static final String SUCCESS_CODE_DATA = "data";
	private static final String GETTOKENURL = "https://ns.huatu.com/u/v1/users/235279560/token";
	private static final String APPINDEXURL = "https://ns.huatu.com/k/v1/points/collectionsByNode?parentId=0";
	String token ="0bc3f13db5824ba8b562546e78964743";

	@Scheduled(cron = "0/15 * * * * *")
	public void scheduled() throws URISyntaxException {
		log.info("start check appindex data");
		HttpHeaders headers = new HttpHeaders();
        headers.add("token", token);
        headers.add("subject", "1");
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(null, headers);

        
		//RequestEntity requestEntityLogin = RequestEntity.get(new URI(APPINDEXURL)).header("token", token).build();


		ResponseEntity<JSONObject> exchange = restTemplate.exchange(APPINDEXURL, HttpMethod.GET, requestEntity, JSONObject.class);
		JSONObject body = exchange.getBody();
		log.info(" appindex data :{}", body.toJSONString());
		if (body.getIntValue(SUCCESS_CODE_KEY) == 1000000) {
			List<Map> object = (List<Map>) body.get(SUCCESS_CODE_DATA);
			if (object.size() == 0) {
				informServiceImpl.informSms("13716429964,18910645425,17611401891", "【华图在线】红色报警：app首页知识点返回数据错误！请及时处理");
				informServiceImpl.informDingDing("app首页知识点返回数据错误！", "", "");
			}

		} else {
			// 登录过期
			RequestEntity requestUserEntity = RequestEntity.get(new URI(GETTOKENURL)).header("secret", "123ztk")
					.build();

			ResponseEntity<JSONObject> userEntity = restTemplate.exchange(requestUserEntity, JSONObject.class);
			JSONObject ret = userEntity.getBody();
			Map object = (Map) ret.get(SUCCESS_CODE_DATA);
			token = (String) object.get("appToken");
			log.info(" get new token :{}", token);

		}

	}

}
