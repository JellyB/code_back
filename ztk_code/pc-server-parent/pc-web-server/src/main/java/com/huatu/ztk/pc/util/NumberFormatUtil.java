package com.huatu.ztk.pc.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class NumberFormatUtil {

	/**
	 * 得到百分比
	 * @param number 被转换的参数
	 * @param scale 小数保留位数
	 * @return
	 */
	public static String getPercent(double number, int scale) {

		DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
		// 可以设置精确几位小数
		df.setMaximumFractionDigits(scale);
		// 模式 例如四舍五入
		df.setRoundingMode(RoundingMode.HALF_UP);
		//double accracy_num = num / total * 100;
		return df.format(number* 100) + "%";

	}

	public static void main(String[] args){
		System.out.println(getPercent(0.3333,2));
	}
}
