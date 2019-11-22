package com.huatu.tiku.teacher.service.impl;

import static com.huatu.tiku.constant.BaseConstant.IMG_PATH;
import static com.huatu.tiku.constant.BaseConstant.IMG_URL;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONObject;
import com.huatu.tiku.constants.WeChatConstant;
import com.huatu.tiku.teacher.service.WeChatService;
import com.huatu.tiku.util.file.UploadFileUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author zhangchong
 *
 */
@Service
@Slf4j
public class WeChatServiceImpl implements WeChatService {

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private RestTemplate restTemplate;

	private String accessTokenRedisKey = "wechat.accessToken";

	@Override
	public String getAccessToken() {
		String accessToken = (String) redisTemplate.opsForValue().get(accessTokenRedisKey);
		if (StringUtils.isNoneBlank(accessToken)) {
			return accessToken;
		}
		Map<String, Object> forObject = restTemplate.getForObject(WeChatConstant.GETACCESSTOKENURL, Map.class);
		if (forObject != null) {
			accessToken = (String) forObject.get("access_token");
			Integer expiresIn = (Integer) forObject.get("expires_in");
			redisTemplate.opsForValue().set(accessTokenRedisKey, accessToken, expiresIn - 60 * 10, TimeUnit.SECONDS);
		}
		return accessToken;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public String getQrCode(String scene) {
		String accessToken = getAccessToken();
		Map<String, Object> params = new HashMap<>();
		// 参数,一个可以直接放入参数例如:1 多个例如：id=1&name=2&...
		params.put("scene", scene);
		// 必须是已经发布的小程序存在的页面（否则报错），例如 pages/index/index, 根路径前不要填加 /
		params.put("path", "pages/home/index");
		// 二维码的宽度，单位 px，最小 280px，最大 1280px
		params.put("width", 430);
		// 自动配置线条颜色，如果颜色依然是黑色，则说明不建议配置主色调，默认 false
		params.put("auto_color", true);
		// auto_color 为 false 时生效，使用 rgb 设置颜色 例如 {"r":"xxx","g":"xxx","b":"xxx"} 十进制表示
		// params.put("line_color", "{\"r\":0,\"g\":0,\"b\":0}");
		// 是否需要透明底色，为 true 时，生成透明底色的小程序
		params.put("is_hyaline", false);
		// 创建请求头
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String jsonString = JSONObject.toJSONString(params);
		log.info("request is :{}", jsonString);
		HttpEntity<String> entity = new HttpEntity<String>(jsonString, headers);
		try {
			ResponseEntity<Resource> responseEntity = restTemplate.postForEntity(
					WeChatConstant.GETWXACODEUNLIMITURL.replace("${ACCESS_TOKEN}", accessToken), entity,
					Resource.class);
			log.info("reponse status is :{}", responseEntity.getStatusCode());
			InputStream in = responseEntity.getBody().getInputStream();
			// 生成新文件名称
			String fileName = UUID.randomUUID().toString() + ".png";
			UploadFileUtil.getInstance().ftpUploadFileInputStream(in, fileName, IMG_PATH);
			return IMG_URL + fileName;
		} catch (RestClientException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
