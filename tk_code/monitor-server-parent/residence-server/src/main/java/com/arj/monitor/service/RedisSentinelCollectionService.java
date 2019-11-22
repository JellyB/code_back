package com.arj.monitor.service;


import java.util.Date;
import java.util.List;
import java.util.Map;

import org.redisson.Redisson;
import org.redisson.api.RBuckets;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RedisSentinelCollectionService {

	private RedissonClient redisson;
	
	@Value("${collect.redis.sentinel.address}")
	private String address;
	
	@Value("${collect.redis.sentinel.mastername}")
	private String masterName;

//	public RedisSentinelCollectionService(@Value("${collect.redis.sentinel.address}") String address,
//			@Value("${collect.redis.sentinel.mastername}") String masterName) {
//		
//
//	}

	public String collect() {
		String msg = "";
		try {
			Config config = new Config();
			List<String> hostList = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(address);
			config.useSentinelServers().setMasterName(masterName)
					.addSentinelAddress(hostList.toArray(new String[hostList.size()]));
			redisson = Redisson.create(config);
			Map map = Maps.newHashMap();
			map.put("monitor-bucket", new Date().getTime());
			RBuckets bucket = redisson.getBuckets();
			bucket.set(map);
			Map<String, Object> retMap = bucket.get("monitor-bucket");
			log.info("sentinel check ret:{}", retMap.get("monitor-bucket"));
		} catch (Exception e) {
			msg = e.getMessage();
			log.error("sentinel check error:{}", e.getMessage());
		}finally {
			if(redisson != null) {
				redisson.shutdown();
			}
		}
		
		return msg;

	}

	public String delUserSession() {
		String msg = "ok";
		try {
			Map map = Maps.newHashMap();
			StringCodec codec = new StringCodec();
			RBuckets bucket = redisson.getBuckets(codec);
			String tokenKey = "";
			for (int i = 0; i < 50; i++) {
				tokenKey = "ic.utoken_" + i;
				Map retMap = bucket.get(tokenKey);
				if (!retMap.isEmpty()) {
					log.info("sentinel check ret:{}", retMap.get(tokenKey));
					String token = (String) retMap.get(tokenKey);
					redisson.getKeys().delete(token);
					redisson.getKeys().delete(tokenKey);
					Map retMapNew = bucket.get(tokenKey);
					log.info("sentinel check retNew:{}",
							retMapNew.isEmpty() ? "删除" + tokenKey : retMapNew.get(tokenKey));

				}
			}
		} catch (Exception e) {
			msg = e.getMessage();
			log.error("sentinel check error:{}", e);
		}
		return msg;

	}

}
