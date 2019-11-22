package com.huatu.tiku.position.biz.util;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 消息工具类
 * 
 * @author Geek-S
 *
 */
@Slf4j
public class SmsUtil {

	/**
	 * 短信最大长度
	 */
	public static final int CONTENT_MAX_LENGTH = 500;

	public static final String MD_API_URL = "http://sdk2.zucp.net/webservice.asmx/mt";

	private static final String SIGNATURE = "【华图在线】";

	private static OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(2000, TimeUnit.MILLISECONDS)
			.writeTimeout(5000, TimeUnit.MILLISECONDS).readTimeout(5000, TimeUnit.MILLISECONDS)
			.connectionPool(new ConnectionPool(100, 5 * 60 * 1000, TimeUnit.MILLISECONDS)).followRedirects(true)// 跟踪重定向
			.build();

	public static void sendSms(String mobile, String body) {
		RequestBody requestBody = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"),
				getBody(Lists.newArrayList(mobile), SIGNATURE + body));
		Request request = new Request.Builder().url(MD_API_URL).post(requestBody).build();
		try {
			okHttpClient.newCall(request).execute();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void sendMultipleSms(List<String> mobiles, String body) {
		RequestBody requestBody = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"),
				getBody(mobiles, SIGNATURE + body));
		Request request = new Request.Builder().url(MD_API_URL).post(requestBody).build();
		try {
			okHttpClient.newCall(request).execute();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param mobiles
	 * @param content
	 * @return
	 */
	private static String getBody(List<String> mobiles, String content) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("sn", "SDK-BBX-010-22650");
		params.put("pwd", "EFF7D598AD84203CB0E74B39562CED54");
		params.put("mobile", Joiner.on(",").join(mobiles));
		try {
			content = URLEncoder.encode(content, "UTF-8");
		}catch (Exception e){
			log.warn("sms exception: {}",e.getMessage());
		}
		params.put("content", content);
		params.put("ext", "");
		params.put("stime", "");
		params.put("rrid", "");
		params.put("msgfmt", "");
		return Joiner.on("&").withKeyValueSeparator("=").join(params);
	}

	/**
	 * 判断短信长度
	 * 
	 * @param source
	 *            源字符串
	 * @param target
	 *            拼接字符串
	 * @return true/false
	 */
	public static boolean checkLength(String source, String target) {
		return StringUtil.getLength(SIGNATURE + source + target) <= CONTENT_MAX_LENGTH;
	}
}