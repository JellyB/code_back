package com.arj.monitor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arj.monitor.common.BaseResult;
import com.arj.monitor.service.EtcdCollectionService;
import com.arj.monitor.service.MongoCollectionService;
import com.arj.monitor.service.MysqlCollectionService;
import com.arj.monitor.service.RabbitMQCollectionService;
import com.arj.monitor.service.RedisClusterCollectionService;
import com.arj.monitor.service.RedisSentinelCollectionService;
import com.arj.monitor.service.SSDBCollectionService;
import com.arj.monitor.service.ZookeeperCollectionService;

@RestController
@RequestMapping("/base")
public class ReportApi {

	@Autowired
	private MongoCollectionService mongoCollectionService;

	@Autowired
	private MysqlCollectionService mysqlCollectionService;

	@Autowired
	private RabbitMQCollectionService mqCollectionService;

	@Autowired
	private RedisClusterCollectionService redisClusterCollectionService;

	@Autowired
	private RedisSentinelCollectionService redisSentinelCollectionService;

	@Autowired
	private EtcdCollectionService etcdCollectionService;

	@Autowired
	private SSDBCollectionService ssdbCollectionService;

	@Autowired
	private ZookeeperCollectionService zookeeperCollectionService;

	/**
	 * redis集群健康检测
	 * 
	 * @return
	 */
	@GetMapping("/redis/checkClusterStatus")
	public BaseResult checkClusterStatus() {
		String ret = redisClusterCollectionService.collect();
		return BaseResult.create(ret);
	}

	/**
	 * redis哨兵健康检测
	 * 
	 * @return
	 */
	@GetMapping("/redis/checkSentinelStatus")
	public BaseResult checkSentinelStatus() {
		String ret = redisSentinelCollectionService.collect();
		return BaseResult.create(ret);
	}

	/**
	 * etcd健康检测
	 * 
	 * @return
	 */
	@GetMapping("/checkEtcdStatus")
	public BaseResult checkEtcdStatus() {
		String ret = etcdCollectionService.collect();
		return BaseResult.create(ret);
	}

	/**
	 * ssdb健康检测
	 * 
	 * @return
	 */
	@GetMapping("/checkSsdbStatus")
	public BaseResult checkSsdbStatus() {
		String ret = ssdbCollectionService.collect();
		return BaseResult.create(ret);
	}

	/**
	 * rabbitmq健康检测
	 * 
	 * @return
	 */
	@GetMapping("/checkMqStatus")
	public BaseResult checkMqStatus() {
		String ret = mqCollectionService.collect();
		return BaseResult.create(ret);
	}

	/**
	 * mysql健康检测
	 * 
	 * @return
	 */
	@GetMapping("/checkMysqlStatus")
	public BaseResult checkMysqlStatus() {
		String ret = mysqlCollectionService.collect();
		return BaseResult.create(ret);
	}

	/**
	 * mongo健康检测
	 * 
	 * @return
	 */
	@GetMapping("/checkMongoStatus")
	public BaseResult checkMongoStatus() {
		String ret = mongoCollectionService.collect();
		return BaseResult.create(ret);
	}

	/**
	 * zookeeper 监控检测
	 * 
	 * @return
	 */
	@GetMapping("/checkZookeeperStatus")
	public BaseResult checkZookeeperStatus() {
		String ret = zookeeperCollectionService.collect();
		return BaseResult.create(ret);
	}

}
