package com.arj.monitor.job;

import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONObject;
import com.arj.monitor.common.RedisConstantKey;
import com.arj.monitor.entity.ServerInfo;
import com.arj.monitor.repository.ServerInfoRepository;
import com.arj.monitor.service.impl.InformServiceImpl;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author zhangchong
 *
 */
@Slf4j
@Component
public class MonitorAppHealth {
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private ServerInfoRepository serverInfoRepository;
	@Autowired
	private RedisTemplate redisTemplate;
	@Resource(name = "informServiceImpl")
	private InformServiceImpl informServiceImpl;

	private static final int SUCCESS_CODE = 1000000;
	private static final String SUCCESS_CODE_KEY = "code";
	private static final String SUCCESS_CODE_DATA = "data";
	private static final String SUCCESS_CODE_UP = "up";
	private static final String SUCCESS_CODE_SUCCESS = "success";
	private static final String SUCCESS_CODE_STATUS = "status";
	private static final String MESSAGE = "message";

	@Scheduled(cron = "0/6 * * * * *")
	public void scheduled() {
		new Thread(() -> {
			List<ServerInfo> serverInfoList = Lists.newArrayList();
			log.info("-------------{}--开始扫描---------------", System.currentTimeMillis());
			// TODO 缓存代码暂时注释，方便调试
			// serverInfoList =
			// redisTemplate.opsForList().range(RedisConstantKey.ALL_SERVER_INFO, 0, -1);

			if (CollectionUtils.isEmpty(serverInfoList)) {
				serverInfoList = serverInfoRepository.findAll(new Sort(Sort.Direction.DESC, "id"));
				if (CollectionUtils.isEmpty(serverInfoList)) {
					return;
				}
				// redisTemplate.opsForList().rightPushAll(RedisConstantKey.ALL_SERVER_INFO,
				// serverInfoList);
			}

			serverInfoList.forEach(serverInfo -> {
				String url = serverInfo.getUrl();
				log.info(url);
				// 健康检查接口 返回格式：{"data":{"status":"UP"},"code":1000000}
				try {
					JSONObject jsonObject = restTemplate.getForObject(url, JSONObject.class);

					if (SUCCESS_CODE == jsonObject.getIntValue(SUCCESS_CODE_KEY)) {
						if (serverInfo.getType() == 0) {
							if (!SUCCESS_CODE_UP.equalsIgnoreCase(
									jsonObject.getJSONObject(SUCCESS_CODE_DATA).get(SUCCESS_CODE_STATUS).toString())) {

								JSONObject details = jsonObject.getJSONObject("data").getJSONObject("details");
								if (null != details) {
									for (Entry<String, Object> entry : details.entrySet()) {
										String paramString = entry.getKey();
										JSONObject json = (JSONObject) entry.getValue();
										String statusString = (String) json.get("status");
										if (!"up".equalsIgnoreCase(statusString)) {
											// 该组件有问题
											log.error("组件{}发生故障{}", paramString, json.toJSONString());
											triggerAlarm(serverInfo, paramString+":"+json.toJSONString());
										}
									}
								}
							}
						} else if (serverInfo.getType() == 1) {
							// 中间件监控状态检查
							// {"code": 1000000,"message": "请求成功","data": null}
							log.info("中间件:{} 状态检查结果{}", serverInfo.getName(), jsonObject.toJSONString());
						}
						// 自定义接口返回格式 {"message":"操作成功","code":1000000} 状态不对第一个if已经过滤了

						// 状态码不对，直接通知
					} else {
						log.info("中间件:{} 状态检查异常{}", serverInfo.getName(), jsonObject.toJSONString());
						triggerAlarm(serverInfo, jsonObject.get(MESSAGE).toString());
					}

				} catch (Exception e) {
					e.printStackTrace();
					triggerAlarm(serverInfo, e.getMessage());
				}
			});
		}).start();

	}

	private void triggerAlarm(ServerInfo serverInfo, String err) {
		long minute = Long.parseLong(DateFormatUtils.format(new Date(), "yyyyMMddHHmm"));

		redisTemplate.opsForValue()
				.increment(RedisConstantKey.getExceptionServerCountStringKey(serverInfo.getId(), minute), 1L);
		int num = (Integer) redisTemplate.opsForValue()
				.get(RedisConstantKey.getExceptionServerCountStringKey(serverInfo.getId(), minute));
		System.err.println("-------------------------------" + num);
		// 设置下过期时间
		if (num == 1) {
			redisTemplate.expire(RedisConstantKey.getExceptionServerCountStringKey(serverInfo.getId(), minute), 2,
					TimeUnit.MINUTES);
		} else if (num >= serverInfo.getFrequency()) {
			informServiceImpl.inform(serverInfo, err, minute);
		}

	}

}
