package com.ht.galaxy.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * 日期格式
 * 
 * @author Geek-S
 *
 */
public class DateFormat {

	public static final String SECOND = "yyyy年MM月dd日 hh时mm分ss秒";

	public static final String MINUTE = "yyyy年MM月dd日 hh时mm分";

	public static final String HOUR = "yyyy年MM月dd日 hh时";

	public static final String DAY = "yyyy年MM月dd日";

	public static final String WEEK = "yyyy年MM月dd日";

	public static final String MONTH = "yyyy年MM月";

	public static final String QUARTER = "yyyy年MM月";

	public static final String YEAR = "yyyy年";

	public static final Map<String, String> DATE_FORMATS;

	static {
		DATE_FORMATS = new HashMap<>();
		DATE_FORMATS.put("1s", SECOND);
		DATE_FORMATS.put("1m", MINUTE);
		DATE_FORMATS.put("1h", HOUR);
		DATE_FORMATS.put("1d", DAY);
		DATE_FORMATS.put("1w", WEEK);
		DATE_FORMATS.put("1M", MONTH);
		DATE_FORMATS.put("1q", QUARTER);
		DATE_FORMATS.put("1y", YEAR);
	}

	/**
	 * 根据ES单位返回对应的日期格式
	 * 
	 * @param key
	 *            ES单位
	 * @return 日期格式
	 */
	public static String getFormat(String key) {
		return DATE_FORMATS.get(key);
	}
}
