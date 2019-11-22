package com.huatu.tiku.course.util;

import java.nio.charset.Charset;
import java.util.TreeMap;

import javax.annotation.Resource;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONObject;
import com.google.common.hash.Hashing;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhangchong
 *
 */
@Component
@Slf4j
public class EncryptUtils {


	String enctype = "application/x-www-form-urlencoded";

	// 获取回调地址
	String getClassCallBackUrl = "https://api.baijiayun.com/openapi/live_account/getClassCallbackUrl";
	
	String setClassCallbackUrl = "https://api.baijiayun.com/openapi/live_account/setClassCallbackUrl";
	
	String callBackUrl = "https://ns-test.htexam.com/c/v6/practice/liveCallBack/liveCallBack";



	String key = "3i0CwHtRQ0APGWlcYglWzwV06dr2ZmjyRX0u0xf9GR0tfLUdDsE4pM3bHjmNIkHUPsnaM14DZpvmpFu8EM0tZvUKexc/af0sJvjh+1aKdplHJ/2DydThSerHj66SGTOy";
	String parentId = "87441664";
	
	

	@Resource
	private RestTemplate restTemplate;

	public JSONObject getClassCallBackUrl() {
		TreeMap<String, Object> treeMap = getParamTree();
		JSONObject jsonObject = this.postForJson(getClassCallBackUrl, treeMap);

		return jsonObject;
	}
	
	public JSONObject setClassCallBackUrl() {
		TreeMap<String, Object> treeMap = getParamTree();
		treeMap.put("url", callBackUrl);
		JSONObject jsonObject = this.postForJson(setClassCallbackUrl, treeMap);

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
		log.info("获取回调地址请求参数:{} 返回值为:{}", treeMap, result);
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
		signParams = signParams + key;
		String sign = Hashing.md5().hashString(signParams, Charset.forName("UTF-8")).toString().toLowerCase();
		return result.append("&sign").append("=").append(sign).toString();
	}

	public TreeMap<String, Object> getParamTree() {
		TreeMap<String, Object> treeMap = new TreeMap<String, Object>();
		 treeMap.put("partner_id",parentId);
		treeMap.put("timestamp", System.currentTimeMillis());
		return treeMap;
	}

}
