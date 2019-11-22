package com.huatu.ztk.user.service;

import java.nio.charset.Charset;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.google.common.hash.Hashing;
import com.huatu.ztk.user.controller.UserSystemControllerV1;

/**
 * Created by lijun on 2019/1/10
 */
@Component
public class ThirdTaskComponent {

	public static final Logger logger = LoggerFactory.getLogger(ThirdTaskComponent.class);

	@Autowired
	private UserServerConfig userServerConfig;

	@Autowired
	private RegisterFreeCourseDetailConfig registerFreeCourseDetailConfig;

	@Autowired
	private RestTemplate restTemplate;

	public String enctype = "application/x-www-form-urlencoded";
	
	/**
	 * PHP同步用户地址
	 */
	public String SYNC_USER_DATA_URL = "/lumenapi/v4/common/user/sync_user_data";

	/**
	 * 用户注册送课
	 *
	 * @param userName 用户名
	 */
	@Async
	public void createUserRegisterOrderV1(String userName) {
		LinkedMultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
		params.add("userName", userName);
		params.add("classId", registerFreeCourseDetailConfig.getRegisterClassIds());
		params.add("secret", UserSystemControllerV1.MD5(userName));
		HttpEntity<LinkedMultiValueMap<String, Object>> httpEntity = new HttpEntity<>(params);
		logger.info(" 用户注册 送课 》》》》params = {},createUserRegisterOrder-userName={},time = {}", params, userName,
				System.currentTimeMillis());
		long l = System.currentTimeMillis();
		try {
			restTemplate.postForObject(userServerConfig.getRegisterCreateOrderUrl() + "/c/v5/order/userRegisterOrder",
					httpEntity, Object.class);
		} catch (RestClientException e) {
			e.printStackTrace();
			logger.error("用户注册 送课 》》》》 error,username:{},classId:{}", userName, userServerConfig.getRegisterClassId());
		}
		logger.info(" 送课 expendTime = {},createUserRegisterOrder-userName={},time = {}", System.currentTimeMillis() - l,
				userName, System.currentTimeMillis());
	}

	@Async
	public void createUserRegisterOrderV2(String userName) {
		TreeMap<String, Object> treeMap = new TreeMap<String, Object>();
		treeMap.put("timestamp", System.currentTimeMillis());
		treeMap.put("userName", userName);
		treeMap.put("classId", registerFreeCourseDetailConfig.getRegisterClassIds());
		treeMap.put("source", 1);
		HttpHeaders httpHeaders = new HttpHeaders();
		MediaType mediaType = MediaType.parseMediaType(enctype);
		httpHeaders.setContentType(mediaType);
		httpHeaders.add("Accept", org.springframework.http.MediaType.APPLICATION_JSON.toString());
		HttpEntity<String> formEntity = new HttpEntity<String>(parseParams(treeMap), httpHeaders);
		try {
			String result = restTemplate.postForObject(
					userServerConfig.getPhpBaseUrl() + "/lumenapi/v4/common/order/batch_zero_order", formEntity,
					String.class);
			logger.info(" 用户注册 送课 params = {},createUserRegisterOrder-userName={},返回结果:{},time = {}", treeMap, userName,
					result, System.currentTimeMillis());
		} catch (RestClientException e) {
			e.printStackTrace();
			logger.error("用户注册 送课  error,username:{},classId:{}", userName, userServerConfig.getRegisterClassId());
		}

	}

	@Async
	public void syncUserData(String email, String mobile, String username) {
		LinkedMultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
		params.add("email", email);
		params.add("mobile", StringUtils.trimToNull(mobile));
		params.add("nick", username);
		params.add("uname", username);
		HttpEntity<LinkedMultiValueMap<String, Object>> httpEntity = new HttpEntity<>(params);
		try {
			long l2 = System.currentTimeMillis();
			restTemplate.postForObject(userServerConfig.getPhpBaseUrl() + SYNC_USER_DATA_URL,
					httpEntity, Object.class);
			long l3 = System.currentTimeMillis();
			logger.info("8-3.createZtkUser sync_user_data - {},account = {},time = {}", l3 - l2, mobile,
					System.currentTimeMillis());
		} catch (RestClientException e) {
			e.printStackTrace();
			logger.error("notify php register user error username:{},account = {}", username, mobile);
		}
	}

	/**
	 * 上报注册用户地理位置
	 * @param uName
	 * @param cv
	 * @param terminal
	 * @param provice
	 * @param city
	 * @param district
	 * @param street
	 * @param positionName
	 * @param device
	 * @param systemVersion
	 */
	@Async
	public void reportRegisterPosition(String phone, String uName, String cv, int terminal, String province, String city,
			String district, String street, String positionName, String device, String systemVersion) {
		LinkedMultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
		params.add("uname", uName);
		params.add("mobile", StringUtils.trimToNull(phone));
		params.add("appVersion", cv);
		params.add("city", city);
		params.add("countyArea", district);
		params.add("detailedAddress", positionName);
		params.add("equipment", device);
		params.add("fromUser", terminal + "");
		params.add("province", province);
		params.add("systemVersion", systemVersion);
		HttpHeaders headers = new HttpHeaders();
	     MediaType type = MediaType.parseMediaType(enctype);
	     headers.setContentType(type);
	     headers.add("Accept", MediaType.APPLICATION_JSON.toString());

		HttpEntity<LinkedMultiValueMap<String, Object>> httpEntity = new HttpEntity<>(params);
		try {
			long l2 = System.currentTimeMillis();
			Object postForObject = restTemplate.postForObject(userServerConfig.getPhpBaseUrl() + SYNC_USER_DATA_URL, httpEntity, Object.class);
			long l3 = System.currentTimeMillis();
			logger.info("reportRegisterPosition sync_user_data ret:{},account = {},time = {}", postForObject, uName,
					l3 - l2, System.currentTimeMillis());
		} catch (RestClientException e) {
			e.printStackTrace();
			logger.error("reportRegisterPosition error username:{}", uName);
		}
	}
	
	/**
	 * 拼接参数
	 * 
	 * @param params
	 * @return
	 */
	public String parseParams(TreeMap<String, Object> params) {
		// 拼接已有参数
		StringBuilder result = new StringBuilder();
		params.entrySet().forEach(param -> {
			result.append(param.getKey()).append("=").append(param.getValue()).append("&");
		});
		// 获取sign
		String signParams = result.toString() + "partner_key=";
		signParams = signParams + registerFreeCourseDetailConfig.getParentKey();
		String sign = Hashing.md5().hashString(signParams, Charset.forName("UTF-8")).toString().toLowerCase();
		return result.append("&sign").append("=").append(sign).toString();
	}


}
