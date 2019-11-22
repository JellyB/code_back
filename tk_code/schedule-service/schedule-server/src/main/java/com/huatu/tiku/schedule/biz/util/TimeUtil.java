package com.huatu.tiku.schedule.biz.util;

import java.math.BigDecimal;

/**
 * 时间工具
 * 
 * @author Geek-S
 *
 */
public class TimeUtil {

	/**
	 * 计算两个时间的时间差
	 * 
	 * @param time0
	 *            时间
	 * @param time1
	 *            时间
	 * @return 时间差（分钟）
	 */
	public static Integer interval(Integer time0, Integer time1) {
		int end = Integer.max(time0, time1);

		int start = Integer.min(time0, time1);

		int startH = start / 100;

		int startM = start % 100;

		int endH = end / 100;

		int endM = end % 100;

		return (endH - startH) * 60 + (endM - startM);
	}

	/**
	 * 分钟转化成小时
	 * 
	 * @param minut
	 *            分钟
	 * @return 小时
	 */
	public static BigDecimal minut2Hour(Integer minut) {
		return new BigDecimal(minut).divide(new BigDecimal(60), 2, BigDecimal.ROUND_DOWN);
	}
	/**
	 * 分钟转化成小时
	 *
	 * @param minut
	 *            分钟
	 * @return 小时
	 */
	public static BigDecimal minut2Hour(String minut) {
		return new BigDecimal(minut).divide(new BigDecimal(60), 2, BigDecimal.ROUND_DOWN);
	}
	/**
	 * 小时转化成分钟
	 *
	 * @param hour
	 *            小时
	 * @return 分钟
	 */
	public static int hour2Minut(String hour) {
		if(hour.length()!=1){
            int i = hour.lastIndexOf(".");
            Double hours = Double.valueOf(hour.substring(0,i))*60;
            Double mins = Double.valueOf(hour.substring(i+1));
            mins=mins/100;
            mins=mins*60;
            return (int)(hours+mins);
        }else {
			return 0;
		}
	}

}
