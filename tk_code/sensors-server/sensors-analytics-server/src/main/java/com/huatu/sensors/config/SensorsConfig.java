package com.huatu.sensors.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sensorsdata.analytics.javasdk.SensorsAnalytics;


/**
 * 神策配置类
 * 
 * @author zhangchong
 *
 */
@Configuration
public class SensorsConfig {

	private static final Logger logger = LoggerFactory.getLogger(SensorsConfig.class);

	@Value("${sensors.analytics.saLogPath}")
	private String saLogPath;


	@Bean
	public SensorsAnalytics sensorsAnalytics() {
		SensorsAnalytics sensorsAnalytics = null;
		try {
			logger.info("SensorsAutoConfiguration init........saLogPath is:" + saLogPath);
			// 检测是否存在日志目录
//			String localPathString = "/Users/zhangchong/Desktop/sensors";
//			File file = new File(localPathString);
			File file = new File(saLogPath == null ? "/data/logs/sa/access_log" : saLogPath);
			if (!file.getParentFile().exists()) {
				new File(file.getParent()).mkdirs();
			}
			//sensorsAnalytics = new SensorsAnalytics(new SensorsAnalytics.DebugConsumer("https://datax-api.huatu.com/log_agent?project=production", true));
			sensorsAnalytics = new SensorsAnalytics(new SensorsAnalytics.ConcurrentLoggingConsumer(saLogPath));
			Map<String, Object> properties = new HashMap<String, Object>();
			// 业务线
			properties.put("business_line", "华图在线");
			// 产品名称
			properties.put("product_name", "华图在线");
			// 域名一级分类
			properties.put("domain_first_classification", "v");
			// 设置事件公共属性
			sensorsAnalytics.registerSuperProperties(properties);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return sensorsAnalytics;
	}
}
