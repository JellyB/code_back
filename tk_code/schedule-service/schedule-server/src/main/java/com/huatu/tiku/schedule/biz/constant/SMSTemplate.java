package com.huatu.tiku.schedule.biz.constant;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;


/**
 * 短信模版
 * 
 * @author geek-s
 *
 */
public class SMSTemplate {

	/**
	 * 课程确认
	 */
	public static final String COURSE_CONFIRM = "您好，%s老师，“%s”课程中给您安排共%s节%s%s任务，请点击以下链接进行确认！%s";

	public static void main(String[] args) {
		List<Object[]> temp = Lists.newArrayList();
		
		Object[] xx = new Object[] { "1", "2", "3", "4", "5" };

		temp.add(xx);
		
		temp.get(0)[1] = 77;
		System.out.println(Arrays.toString(temp.get(0)));
		System.out.println(String.format(COURSE_CONFIRM, xx));
	}
}
