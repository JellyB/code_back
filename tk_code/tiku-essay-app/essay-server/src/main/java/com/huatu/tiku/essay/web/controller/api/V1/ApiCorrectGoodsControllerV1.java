package com.huatu.tiku.essay.web.controller.api.V1;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.service.CorrectGoodsService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.vo.resp.EssayCorrectGoodsVO;
import com.huatu.tiku.springboot.users.support.Token;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("api/v1/goods")
/**
 * 批改商品
 */

public class ApiCorrectGoodsControllerV1 {
	@Autowired
	CorrectGoodsService correctGoodsService;
	@Autowired
	StringRedisTemplate stringRedisTemplate;

	/**
	 * 批改商品列表
	 * 
	 * @param userSession
	 * @param page
	 * @param pageSize
	 * @return
	 */
	@LogPrint
	@GetMapping(value = "correctGoodsList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public List<EssayCorrectGoodsVO> correctGoods(@Token(check = false) UserSession userSession,
			@RequestParam(name = "page", defaultValue = "1") int page,
			@RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {
		PageRequest pageRequest = new PageRequest(page - 1, pageSize, Sort.Direction.ASC, "type");
		List<EssayCorrectGoodsVO> essayCorrectGoodsList = correctGoodsService.list(pageRequest);
		return essayCorrectGoodsList;
	}

	/**
	 * 获取支付宝签名字符串
	 * 
	 * @param userSession
	 * @param orderInfo
	 * @param flag
	 * @return
	 */
	@LogPrint
	@PostMapping(value = "getAliPaySign", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public Object correctGoods(@Token(check = false) UserSession userSession, @RequestBody Map<String, String> orderInfo,
			@RequestParam(name = "flag", defaultValue = "0") Integer flag) {
		Assert.notNull(orderInfo, "签名字符串不能为空");
		return correctGoodsService.getAliPaySign(orderInfo.get("orderInfo"), flag);
	}
	
	@LogPrint
	@PostMapping(value = "getAliPaySignV2", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public Object getAliPaySignV2(@Token(check = false) UserSession userSession, @RequestBody Map<String, String> orderInfo,
			@RequestParam(name = "flag", defaultValue = "0") Integer flag) {
		Assert.notNull(orderInfo, "签名字符串不能为空");
		return correctGoodsService.getAliPaySignV2(orderInfo.get("orderInfo"), flag);
	}
	

}
