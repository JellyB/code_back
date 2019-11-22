package com.huatu.ztk.user.service;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.user.bean.WeChatSessionVO;
import com.huatu.ztk.user.common.WeChatConfig;
import com.huatu.ztk.user.common.WeChatSession;

/**
 * 微信相关
 * 
 * @author zhangchong
 *
 */
@Service
public class WechatService {
	private static final Logger log = LoggerFactory.getLogger(WechatService.class);

	@Resource(name = "redisTemplate")
	private RedisTemplate<String, String> redisTemplate;

	@Autowired
	private RestTemplate restTemplate;

	/**
	 * 生成小程序sessionkey
	 * @param code
	 * @return
	 * @throws BizException
	 */
	public Object code2Session(String code) throws BizException {
		ResponseEntity<String> entity = restTemplate.getForEntity(WeChatConfig.AUTH_RUL.replace("${code}", code),
				String.class);
		String ret = entity.getBody();
		log.info("code2Session code :{},return:{}", code, ret);
		WeChatSession wechat = JsonUtil.toObject(ret, WeChatSession.class);
		log.info("wechat info:{}", wechat.toString());
		if (wechat != null && wechat.getErrcode() == null) {
			redisTemplate.opsForValue().set(wechat.getOpenid(), wechat.getSession_key());
			redisTemplate.expire(wechat.getOpenid(), 7, TimeUnit.DAYS);
			WeChatSessionVO vo = new WeChatSessionVO();
			BeanUtils.copyProperties(wechat, vo);
			return vo;
		} else {
			/**
			 * code说明 -1 系统繁忙，此时请开发者稍候再试 0 请求成功 40029 code 无效 45011 频率限制，每个用户每分钟100次
			 */
			throw new BizException(ErrorResult.create(wechat.getErrcode(), wechat.getErrmsg()));
		}

	}

}
