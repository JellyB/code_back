package com.arj.monitor.util;


import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MysqlConnectionUtils {

	private static Properties pros = null;
	private static Connection conn = null;

	public MysqlConnectionUtils(@Value("${collect.mysql.url}") String url,
			@Value("${collect.mysql.username}") String username, @Value("${collect.mysql.password}") String password) {
		pros = new Properties();
		pros.put("url", url);
		pros.put("username", username);
		pros.put("password", password);
	}

	public static Connection getConn() {
		try {
			if (null == conn || conn.isClosed()) {
				conn = DriverManager.getConnection(pros.getProperty("url"), pros.getProperty("username"),
						pros.getProperty("password"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conn;
	}

}
