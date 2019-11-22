package com.huatu.tiku.course.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;

/**
 * 
 * @author zhangchong
 *
 */
public class DateUtil {

	public static String ERRORSTR = "0000-00-00 00:00:00";

	public static String getSimpleDate(String startTime, String endTime) {
		if (ERRORSTR.equals(startTime.trim()) || ERRORSTR.equals(endTime.trim()) || "".equals(startTime)
				|| "".equals(endTime)) {
			return "";
		}
		DateTimeFormatter dtfOld = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm");
		DateTimeFormatter dtfShort = DateTimeFormatter.ofPattern("MM月dd日 HH:mm");
		DateTimeFormatter dtfDay = DateTimeFormatter.ofPattern("dd日 HH:mm");
		DateTimeFormatter dtfMin = DateTimeFormatter.ofPattern("HH:mm");
		LocalDateTime startDate = LocalDateTime.parse(startTime, dtfOld);
		LocalDateTime endDate = LocalDateTime.parse(endTime, dtfOld);
		if (startDate.getYear() == endDate.getYear()) {
			// 年相等判断月是否相等
			if (startDate.getMonthValue() == endDate.getMonthValue()) {
				if (startDate.getDayOfMonth() == endDate.getDayOfMonth()) {
					// 日相等
					return new StringBuilder(startDate.format(dtfShort)).append("-").append(endDate.format(dtfMin))
							.toString();
				} else {
					// 月相等
					return new StringBuilder(startDate.format(dtfShort)).append("-").append(endDate.format(dtfDay))
							.toString();
				}
			} else {
				// 返回 月-日 - 月-日
				return new StringBuilder(startDate.format(dtfShort)).append("-").append(endDate.format(dtfShort))
						.toString();
			}
		} else {
			return new StringBuilder(startDate.format(dtf)).append("-").append(endDate.format(dtf)).toString();
		}

	}
	
	/**
	 * 判断时间是否过期 
	 * @param endTime  yyyy-MM-dd HH:mm:ss格式
	 * @return
	 */
	public static boolean isExpired(String endTime) {
		if(StringUtils.isEmpty(endTime) || ERRORSTR.equals(endTime.trim())) {
			return false;
		}
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime startDate = LocalDateTime.parse(endTime, dtf);
		Long  endLong = startDate.toInstant(ZoneOffset.of("+8")).toEpochMilli();
		if(endLong > System.currentTimeMillis()) {
			return false;
		}
		return true;
	}

//	public static void main(String[] args) {
//		System.out.println(getSimpleDate("2018-02-10 17:20:20", "2019-02-10 17:20:20"));
//		System.out.println(getSimpleDate("2019-02-10 17:20:20", "2019-03-10 17:20:20"));
//		System.out.println(getSimpleDate("2019-02-10 17:20:20", "2019-02-11 17:20:20"));
//		System.out.println(getSimpleDate("2019-02-11 17:20:20", "2019-02-11 17:21:20"));
//	}

//	public static void main(String[] args) {
//		System.out.println(isExpired("2019-03-18 11:56:00"));
//	}
}
