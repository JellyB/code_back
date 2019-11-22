package com.arj.monitor.service;


import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.springframework.stereotype.Service;

import com.arj.monitor.util.MysqlConnectionUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MysqlCollectionService {

	private final String sql = "select now() from dual";

	public String collect() {
		String ret = "";
		QueryRunner queryRunner = new QueryRunner();

		try {
			Map<String, Object> map = queryRunner.query(MysqlConnectionUtils.getConn(), sql, new MapHandler());
			for (String row : map.keySet()) {
				log.info("check mysql ret:{}", map.get(row));
			}
		} catch (SQLException e) {
			log.error("check mysql err:{}", e);
			ret = e.getMessage();
		}
		return ret;
	}
}
