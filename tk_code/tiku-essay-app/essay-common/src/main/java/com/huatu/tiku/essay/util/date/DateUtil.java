package com.huatu.tiku.essay.util.date;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

/**
 * @author zhouwei
 * @Description: 日期工具类
 * @create 2018-01-27 下午5:57
 **/
public class DateUtil {
	private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("YYYYMMdd");
	
	private static final DateTimeFormatter dateTimeFormatterStyle = DateTimeFormatter.ofPattern("YYYY-MM-dd");

	/**
	 * 当天凌晨的00：00：00 的时间戳
	 *
	 * @return
	 */
	public static long getTodayStartMillions() {
		long now = System.currentTimeMillis() / 1000L;
		long daySecond = 60 * 60 * 24;
		return (now - (now + 8 * 3600) % daySecond) * 1000;
	}

	/**
	 * 当天凌晨的00：00：00 的时间戳
	 *
	 * @return
	 */
	public static long getTodayEndMillions() {
		long now = System.currentTimeMillis() / 1000L;
		long daySecond = 60 * 60 * 24;
		return (now - (now + 8 * 3600) % daySecond + daySecond) * 1000;
	}

	public static String getFormatDateString(long date) {
		if (date != 0) {
			return dateTimeFormatter
					.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneId.of("Asia/Shanghai")));
		} else {
			return "";
		}
	}
	/**
	 * 返回YYYY-MM-dd格式
	 * @param date
	 * @return
	 */
	public static String getFormatDateStyleString(long date) {
		if (date != 0) {
			return dateTimeFormatterStyle
					.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneId.of("Asia/Shanghai")));
		} else {
			return "";
		}
	}
	

	public static void main(String[] args) {
		System.out.println(getTodayEndMillions());
	}

	/**
	 * 获得当前时间的前n小时
	 *
	 * @param
	 * @return
	 * @throws Exception
	 */
	public static Date getHoursBefore(int hours) {
		Date date = new Date();
		Calendar c = Calendar.getInstance();

		c.setTime(date);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		c.set(Calendar.HOUR_OF_DAY, hour - hours);

		return c.getTime();
	}

	/**
	 * 获得指定时间的前n分钟
	 *
	 * @param
	 * @return
	 * @throws Exception
	 */
	public static Date getMinutesBeforeDate(Date date, int minutes) {
		Calendar c = Calendar.getInstance();

		c.setTime(date);
		int minute = c.get(Calendar.MINUTE);
		c.set(Calendar.MINUTE, minute - minutes);

		return c.getTime();
	}

	/**
	 * 获得当前时间的前n分钟
	 *
	 * @param
	 * @return
	 * @throws Exception
	 */
	public static Date getMinutesBefore(int minutes) {
		Date date = new Date();
		Calendar c = Calendar.getInstance();

		c.setTime(date);
		int minute = c.get(Calendar.MINUTE);
		c.set(Calendar.MINUTE, minute - minutes);

		return c.getTime();
	}

	/**
	 * 获得当前时间的前n天
	 *
	 * @param
	 * @return
	 * @throws Exception
	 */
	public static Date getDaysBefore(Date date, int days) {
		Calendar c = Calendar.getInstance();

		c.setTime(date);
		int day = c.get(Calendar.DAY_OF_YEAR);
		c.set(Calendar.DAY_OF_YEAR, day - days);

		return c.getTime();
	}

	public static String convertDateFormat(Date source) {
		// 日期格式转换
		StringBuilder dateStr = new StringBuilder();
		if (null != source) {
			Calendar cal1 = Calendar.getInstance();
			cal1.setTime(source);
			int year = cal1.get(Calendar.YEAR);
			int month = 1 + source.getMonth();
			int date = source.getDate();
			int hours = source.getHours();
			int minutes = source.getMinutes();

			Date now = new Date();
			Calendar cal2 = Calendar.getInstance();
			cal2.setTime(now);
			int nowYear = cal2.get(Calendar.YEAR);
			if (year != nowYear) {
				dateStr.append(year).append("年");
			}
			dateStr.append(month).append("月");
			dateStr.append(date).append("日");
			dateStr.append(" ").append(hours).append(":");
			if (minutes < 10) {
				dateStr.append("0").append(minutes);
			} else {
				dateStr.append(minutes);
			}
		}
		return dateStr.toString();
	}

	/**
	 * 获得“ 某天” 00:00:01
	 */
	public static Long getZeroPointTimestamps(Long timestamps) {
		Long oneDayTimestamps = Long.valueOf(60 * 60 * 24 * 1000);
		long time = timestamps % oneDayTimestamps;
		return timestamps - time - (8 * 60 * 60 * 1000) + 1000;
	}

	/**
	 * 获得“ 某天”23:59:59
	 */
	public static Long getEndPointTimestamps(Long timestamps) {
		Long oneDayTimestamps = Long.valueOf(60 * 60 * 24 * 1000);
		long time = timestamps % oneDayTimestamps;
		return timestamps - time - (8 * 60 * 60 * 1000) + oneDayTimestamps - 1000;
	}

	/**
	 * 获取两天间隔的天数
	 */
	public static double getDays(Long start, Long end) {

		Long oneDayTimestamps = Long.valueOf(60 * 60 * 24 * 1000);
		if (start == end) {
			return 0L;
		} else {
			long time = end - start;
			double days = (time / (double) oneDayTimestamps);
			return Math.ceil(days);
		}

	}

}
