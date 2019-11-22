package com.huatu.ztk.user.utils;

import java.nio.charset.Charset;
import java.util.TreeMap;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONObject;
import com.google.common.hash.Hashing;
import com.huatu.ztk.user.service.RegisterFreeCourseDetailConfig;

import lombok.extern.slf4j.Slf4j;

/**
 * php加密工具类
 */
@Component
@Slf4j
public class EncryptUtils {

	@Autowired
	private RegisterFreeCourseDetailConfig registerFreeCourseDetailConfig;

	String enctype = "application/x-www-form-urlencoded";

	String PHPURL= "http://testapi.huatu.com/lumenapi/v4/common/order/batch_zero_order";

	@Resource
	private RestTemplate restTemplate;

	
	public JSONObject getphp() {
		TreeMap<String, Object> treeMap = getParamTree();
		treeMap.put("userName", "app_ztk263359121");
		treeMap.put("classId", 97361);
		treeMap.put("source", 1);
		JSONObject jsonObject = this.postForJson(PHPURL, treeMap);

		return jsonObject;
	}

	/**
	 * 
	 * @param url
	 * @param treeMap
	 * @return
	 */
	public JSONObject postForJson(String url, TreeMap<String, Object> treeMap) {
		HttpHeaders httpHeaders = new HttpHeaders();
		MediaType mediaType = MediaType.parseMediaType(enctype);
		httpHeaders.setContentType(mediaType);
		httpHeaders.add("Accept", org.springframework.http.MediaType.APPLICATION_JSON.toString());
		HttpEntity<String> formEntity = new HttpEntity<String>(parseParams(treeMap), httpHeaders);
		String result = restTemplate.postForObject(url, formEntity, String.class);
		log.info("获取送课请求参数:{} 返回值为:{}", treeMap, result);
		JSONObject jsonObject = JSONObject.parseObject(result);
		return JSONObject.parseObject(jsonObject.get("data").toString());
	}

	public String parseParams(TreeMap<String, Object> params) {
		// 拼接已有参数
		StringBuilder result = new StringBuilder();
		params.entrySet().forEach(param -> {
			result.append(param.getKey()).append("=").append(param.getValue()).append("&");
		});
		// 获取sign
		String signParams = result.toString() + "partner_key=";
		signParams = signParams +registerFreeCourseDetailConfig.getParentKey();
		String sign = Hashing.md5().hashString(signParams, Charset.forName("UTF-8")).toString().toLowerCase();
		return result.append("&sign").append("=").append(sign).toString();
	}

	public TreeMap<String, Object> getParamTree() {
		TreeMap<String, Object> treeMap = new TreeMap<String, Object>();
		treeMap.put("timestamp", System.currentTimeMillis());
		return treeMap;
	}

}
