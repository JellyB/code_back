package com.huatu.tiku.schedule.function;

import java.util.Calendar;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.huatu.tiku.schedule.biz.util.DateformatUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * Calendar方法测试
 * 
 * @author Geek-S
 *
 */
@Slf4j
public class CalendarT {

	@Test
	public void roll() {
		Calendar now = Calendar.getInstance();

		List<String> dates = Lists.newArrayList();

		dates.add(DateformatUtil.format2(now.getTime()));

		for (int i = 0; i < 31; i++) {
			now.roll(Calendar.DAY_OF_MONTH, true);

			dates.add(DateformatUtil.format2(now.getTime()));
		}

		log.info(dates.toString());
	}
}
