package com.huatu.ztk.paper.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import com.baidu.disconf.client.common.annotations.DisconfItem;
import com.sensorsdata.analytics.javasdk.SensorsAnalytics;

/**
 * 神策配置类
 * 
 * @author zhangchong
 *
 */
@Configuration
@DependsOn("disconfMgrBean2")
public class SensorsAutoConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(SensorsAutoConfiguration.class);

	private String saLogPath;

	@DisconfItem(key = "sensors.analytics.saLogPath")
	public String getSaLogPath() {
		return saLogPath;
	}

	public void setSaLogPath(String saLogPath) {
		this.saLogPath = saLogPath;
	}

	@Bean
	public SensorsAnalytics sensorsAnalytics() {
		SensorsAnalytics sensorsAnalytics = null;
		try {
			logger.info("SensorsAutoConfiguration init........saLogPath is:" + saLogPath);
			// 检测是否存在日志目录
			File file = new File(saLogPath == null ? "/data/logs/paper-web-server/sa/access_log" : saLogPath);
			if (!file.getParentFile().exists()) {
				new File(file.getParent()).mkdirs();
			}
			sensorsAnalytics = new SensorsAnalytics(new SensorsAnalytics.ConcurrentLoggingConsumer(saLogPath));
			Map<String, Object> properties = new HashMap<String, Object>();
			// 业务线
			properties.put("business_line", "华图在线");
			// 产品名称
			properties.put("product_name", "华图在线");
			// 域名一级分类
			properties.put("domain_first_classification", "v");
			// 域名二级分类
			//properties.put("domain_second_classification", "");
			// 域名三级分类
			//properties.put("domain_third_classification", "");
			// 设置事件公共属性
			sensorsAnalytics.registerSuperProperties(properties);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return sensorsAnalytics;
	}
}
