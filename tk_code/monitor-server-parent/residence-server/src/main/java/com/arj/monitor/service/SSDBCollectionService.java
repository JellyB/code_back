package com.arj.monitor.service;

import java.io.IOException;
import java.util.Date;

import org.nutz.ssdb4j.SSDBs;
import org.nutz.ssdb4j.spi.Response;
import org.nutz.ssdb4j.spi.SSDB;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SSDBCollectionService {

	private SSDB ssdb;
	@Value("${collect.ssdb.masterHost}")
	private String masterHost;
	@Value("${collect.ssdb.masterPort}")
	private String masterPort;
	@Value("${collect.ssdb.slaveHost}")
	private String slaveHost;
	@Value("${collect.ssdb.slavePort}")
	private String slavePort;

	// public SSDBCollectionService(@Value("${collect.ssdb.masterHost}") String
	// masterHost,
	// @Value("${collect.ssdb.masterPort}") String masterPort,
	// @Value("${collect.ssdb.slaveHost}") String slaveHost,
	// @Value("${collect.ssdb.slavePort}") String slavePort) {
	// // ssdb = SSDBs.replication(masterHost, Integer.parseInt(masterPort),
	// slaveHost,
	// // Integer.parseInt(slavePort), 5000,
	// // null);
	// ssdb = SSDBs.simple(masterHost, Integer.parseInt(masterPort), 5000);
	// }

	public String collect() {
		String ret = "";
		try {
			ssdb = SSDBs.simple(masterHost, Integer.parseInt(masterPort), 5000);
			ssdb.set("monitor-ssdb-key", new Date().getTime()).check();

			Response resp = ssdb.get("monitor-ssdb-key");
			if (resp.ok()) {
				log.info("ssdb check ret:{}" + resp.asString());
			}
		} catch (Exception e) {
			ret = e.getMessage();
			log.error("ssdb check error:{}", e);
		} finally {
			try {
				ssdb.close();
			} catch (IOException e) {
				log.error("ssdb client close error:{}", e);
			}
		}
		return ret;

	}

}
