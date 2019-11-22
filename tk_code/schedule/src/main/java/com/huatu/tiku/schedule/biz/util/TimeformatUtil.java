package com.huatu.tiku.schedule.biz.util;

/**
 * 时间格式化
 * 
 * @author Geek-S
 *
 */
public class TimeformatUtil {

	/**
	 * Integer类型时间转为HH:mm
	 * 
	 * @param time
	 *            时间
	 * @return 格式化时间
	 */
	public static String format(Integer time) {
		StringBuilder timeString = new StringBuilder(time.toString());

		while (timeString.length() < 4) {
			timeString.insert(0, "0");
		}

		return timeString.insert(2, ":").toString();
	}

}
