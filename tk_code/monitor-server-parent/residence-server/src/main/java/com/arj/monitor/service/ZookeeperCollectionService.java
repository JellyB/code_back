package com.arj.monitor.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.zkclient.IZkClient;
import com.github.zkclient.ZkClient;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ZookeeperCollectionService {

	@Value("${collect.zookeeper.address}")
	private String hostStr;

	@Value("${collect.zookeeper.monitorNode}")
	private String monitorNode;//

	public String collect() {
		String ret = "";
		IZkClient zkClient = null;
		try {
			zkClient = new ZkClient(hostStr);
			List<String> nodes = zkClient.getChildren(monitorNode);
			if (nodes != null && nodes.size() >= 1) {
				log.info("zookeeper is running");
			} else {
				ret = "UserDubboService providers not exists";
			}
		} catch (Exception e) {
			ret = e.getMessage();
			log.error("zookeeper check error:{}", e);
		} finally {
			if(zkClient != null && zkClient.isConnected()) {
				zkClient.close();
			}
		}

		return ret;
	}

//	public static void main(String[] args) throws IOException, TimeoutException {
//
//		IZkClient zkClient = new ZkClient("192.168.100.110:2181,192.168.100.111:2181,192.168.100.112:2181");
//		int count = zkClient.countChildren("/");
//		List<String> nodes = zkClient.getChildren("/dubbo/com.huatu.ztk.user.dubbo.UserDubboService");
//		if (nodes != null && nodes.size() >= 1) {
//			System.out.println("zookeeper is running");
//		} else {
//			System.out.println("zookeeper is not running");
//		}
//		System.out.println(nodes.size());
//		nodes.forEach(a -> {
//			System.err.println(a);
//		});
//
//		// String node = zkClient.create("/monitor-test", null, CreateMode.EPHEMERAL);
//		// System.out.println(zkClient.exists("/"));
//
//	}

}
