package com.huatu.tiku.teacher;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.huatu.tiku.TikuBaseTest;
import com.huatu.tiku.teacher.service.WeChatService;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author zhangchong
 *
 */
@Slf4j
public class WechatServiceTest extends TikuBaseTest {
	@Autowired
	private WeChatService weChatService;
	

	@Test
	public void test() {
		Object qrCode = weChatService.getQrCode("1,1,-9");
		log.info(qrCode.toString());
	}

}
