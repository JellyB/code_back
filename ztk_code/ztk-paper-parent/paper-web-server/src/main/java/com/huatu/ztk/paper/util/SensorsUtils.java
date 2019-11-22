package com.huatu.ztk.paper.util;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.huatu.common.consts.TerminalType;

/**
 * 神策工具类
 * 
 * @author zhangchong
 *
 */
public class SensorsUtils {
	private static final Logger logger = LoggerFactory.getLogger(SensorsUtils.class);

	private static ThreadLocal<Map<String, Object>> THREAD_LOCAL_LOG = new ThreadLocal<Map<String, Object>>();

	public static void setMessage(String key, Object value) {
		Map<String, Object> logMap = THREAD_LOCAL_LOG.get();
		if (logMap == null) {
			logMap = Maps.newHashMap();
		}
		logMap.put(key, value);
		THREAD_LOCAL_LOG.set(logMap);
	}

	public static Map<String, Object> getMessage() {
		Map<String, Object> logMap = THREAD_LOCAL_LOG.get();
		if (logMap == null) {
			logMap = Maps.newHashMap();
		}
		return logMap;

	}

	public static void removeMessage() {
		THREAD_LOCAL_LOG.remove();

	}

	public static String getPlatform(int terminal) {

		switch (terminal) {

		case TerminalType.ANDROID:
		case TerminalType.ANDROID_IPAD:
			return "AndroidApp";

		case TerminalType.IPHONE:
		case TerminalType.IPHONE_IPAD:
			return "iOSApp";

		case TerminalType.PC:
			return "PC";
		case TerminalType.WEI_XIN:
			return "H5";
		case TerminalType.WEI_XIN_APPLET:
			return "小程序";
		default:
			break;
		}
		return "其他";
	}

}
