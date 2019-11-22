package com.arj.monitor.service;


import java.util.Date;
import java.util.List;

import org.redisson.Redisson;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.base.Splitter;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RedisClusterCollectionService {

	private RedissonClient redisson;
	
	@Value("${collect.redis.cluster.address}") 
	private String address;

//	public RedisClusterCollectionService(@Value("${collect.redis.cluster.address}") String address) {
//		Config config = new Config();
//		List<String> hostList = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(address);
//		config.useClusterServers().addNodeAddress(hostList.toArray(new String[hostList.size()]));
//		redisson = Redisson.create(config);
//
//	}

	public String collect() {
		
		String msg = "";
		try {
			Config config = new Config();
			List<String> hostList = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(address);
			config.useClusterServers().addNodeAddress(hostList.toArray(new String[hostList.size()]));
			redisson = Redisson.create(config);
			RSet<String> mySet = redisson.getSet("monitor-set");
			if (mySet != null) {
				mySet.clear();
			}
			mySet.add(new Date().getTime() + "");

			RSet<String> mySetCache = redisson.getSet("monitor-set");

			for (String s : mySetCache) {
				log.info("cluster check ret:{}", s);
			}
			return msg;
		} catch (Exception e) {
			log.error("check redis cluster error:{}", e.getMessage());
			msg = e.getMessage();
			return msg;
		}finally {
			redisson.shutdown();
		}

	}

}
