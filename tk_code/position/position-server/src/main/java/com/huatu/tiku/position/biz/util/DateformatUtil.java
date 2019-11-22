package com.huatu.tiku.position.biz.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 日期格式化
 * 
 * @author Geek-S
 *
 */
public class DateformatUtil {

	private static final SimpleDateFormat SDF0 = new SimpleDateFormat("yyyy-MM-dd");

	private static final SimpleDateFormat SDF1 = new SimpleDateFormat("yyyyMMdd");

	private static final SimpleDateFormat SDF2 = new SimpleDateFormat("MM月dd日");

	private static final SimpleDateFormat SDF3 = new SimpleDateFormat("MMM");

	private static final SimpleDateFormat SDF4 = new SimpleDateFormat("EEE",new Locale("zh","CN"));

	private static final SimpleDateFormat SDF5 = new SimpleDateFormat("yyyy年MM月dd日");

	private static final SimpleDateFormat SDF6 = new SimpleDateFormat("yyyy年MM月dd日HH:mm");

	private static final SimpleDateFormat SDF7 = new SimpleDateFormat("yyyy.MM.dd HH:mm");

	/**
	 * Pattern : yyyy-MM-dd
	 * 
	 * @param date
	 *            日期
	 * @return 格式化字符串
	 */
	public static String format0(Date date) {
		return SDF0.format(date);
	}

	/**
	 * Pattern : yyyy-MM-dd
	 * 
	 * @param date
	 *            日期字符串
	 * @return 日期
	 */
	public static Date parse0(String date) {
		try {
			return SDF0.parse(date);
		} catch (ParseException e) {
			return null;
		}
	}

	/**
	 * Pattern : yyyyMMdd
	 * 
	 * @param date
	 *            日期
	 * @return 格式化字符串
	 */
	public static String format1(Date date) {
		return SDF1.format(date);
	}

	/**
	 * Pattern : yyyyMMdd
	 * 
	 * @param date
	 *            日期字符串
	 * @return 日期
	 */
	public static Date parse1(String date) {
		try {
			return SDF1.parse(date);
		} catch (ParseException e) {
			return null;
		}
	}

	/**
	 * Pattern : MM月dd日
	 * 
	 * @param date
	 *            日期
	 * @return 格式化字符串
	 */
	public static String format2(Date date) {
		return SDF2.format(date);
	}

	/**
	 * Pattern : MMM（四月、七月）
	 * 
	 * @param date
	 *            日期
	 * @return 格式化字符串
	 */
	public static String format3(Date date) {
		return SDF3.format(date);
	}

	/**
	 * Pattern : （星期四、星期日）
	 *
	 * @param date
	 *            日期
	 * @return 格式化字符串
	 */
	public static String format4(Date date) {
		return SDF4.format(date);
	}

	/**
	 * Pattern : （yyyy年MM月dd日）
	 *
	 * @param date
	 *            日期
	 * @return 格式化字符串
	 */
	public static String format5(Date date) {
		return SDF5.format(date);
	}

	/**
	 * Pattern : yyyy年MM月dd日HH:mm
	 *
	 * @param date
	 *            日期
	 * @return 格式化字符串
	 */
	public static String format6(Date date) {
		return SDF6.format(date);
	}

	public static String format7(Date date) {
		return SDF7.format(date);
	}

	public static Date getDateFromSDF5(String date) {
		try {
			return SDF5.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

}
