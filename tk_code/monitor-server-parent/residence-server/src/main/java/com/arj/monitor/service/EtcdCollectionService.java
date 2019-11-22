package com.arj.monitor.service;


import java.io.IOException;
import java.net.URI;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.responses.EtcdKeysResponse;

@Service
@Slf4j
public class EtcdCollectionService {

	private EtcdClient etcdClient;

//	public EtcdCollectionService() {
//		etcdClient = new EtcdClient(URI.create("http://etcd01:2379"), URI.create("http://etcd02:2379"),
//				URI.create("http://etcd03:2379"));
//		log.info("etcd check version:{}", etcdClient.version());
//	}

	public String collect() {
		String ret = "";
		try {
			etcdClient = new EtcdClient(URI.create("http://etcd01:2379"), URI.create("http://etcd02:2379"),
					URI.create("http://etcd03:2379"));
			EtcdKeysResponse response = etcdClient.put("monitor-foo", "bar").send().get();
			log.info("etcd check ret:{}", response.node.value);
		} catch (Exception e) {
			ret = e.getMessage();
			log.error("etcd check error:{}", e);
		}finally {
			try {
				etcdClient.close();
			} catch (IOException e) {
				log.error("etcd client close error:{}", e);
			}
		}
		return ret;

	}

}
