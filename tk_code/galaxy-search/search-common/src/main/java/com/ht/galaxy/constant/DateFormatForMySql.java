package com.ht.galaxy.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jbzm
 * @date Create on 2018/4/10 11:37
 */
public class DateFormatForMySql {

	public static final String SECOND = "yyyy-MM-dd- hh-mm-ss";

	public static final String MINUTE = "yyyy-MM-dd hh-mm";

	public static final String HOUR = "yyyy-MM-dd hh";

	public static final String DAY = "yyyy-MM-dd";

	public static final String WEEK = "yyyy-MM-dd";

	public static final String MONTH = "yyyy-MM";

	public static final String QUARTER = "yyyy-MM";

	public static final String YEAR = "yyyy";

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
	 * 根据ES单位返回对应的期格式
	 * 
	 * @param key
	 *            ES单位
	 * @return 期格式
	 */
	public static String getFormat(String key) {
		return DATE_FORMATS.get(key);
	}
}
