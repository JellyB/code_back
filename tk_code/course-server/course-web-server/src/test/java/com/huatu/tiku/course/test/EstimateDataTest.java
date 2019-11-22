package com.huatu.tiku.course.test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import com.huatu.common.test.BaseWebTest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EstimateDataTest extends BaseWebTest {

	@Autowired
	private RestTemplate resttemplate;

	@Test
	public void getActivityData() {
		String listUrl = "http://ns.huatu.com/pand/activityList/list?areaIds=&type=8&bizStatus=2&name=0420&subjectId=1&page=1&size=50&year=";
		String itemDataUrl = "http://ns.huatu.com/pand/activityList/activityData?id=%s";

		Map forObject = resttemplate.getForObject(listUrl, Map.class);
		Map<String, Object> data = (Map<String, Object>) forObject.get("data");
		List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");
		int total = 0;
		log.error("数据统计日期:{}", LocalDateTime.now());
		for (int i = 0; i < list.size(); i++) {
			String areaNames = (String) list.get(i).get("areaNames");
			Integer paperId = (Integer) list.get(i).getOrDefault("id", 0);
			Map forObjectData = resttemplate.getForObject(String.format(itemDataUrl, paperId), Map.class);
			Map<String, Object> itemData = (Map<String, Object>) forObjectData.get("data");
			Integer count = (Integer) itemData.get("participants");
			total += count;
			log.error("试卷id:{},地区:{},参加人数:{}", paperId, areaNames, count);

		}
		log.error("共{}场估分,参加人数:{}", list.size(), total);
	}

}
