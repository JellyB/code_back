package com.huatu.tiku.essay.web.controller.api.V3;

import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.service.CorrectGoodsService;
import com.huatu.tiku.essay.service.correct.CorrectGoodsServiceV1;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.vo.resp.EssayCorrectGoodsVO;
import com.huatu.tiku.springboot.users.support.Token;
import lombok.extern.slf4j.Slf4j;
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

import java.util.List;
import java.util.Map;

/**
 * @author huangqingpeng
 * @title: ApiCorrectGoodsControllerV3
 * @description: 批改次数上屏列表返回查询（添加人工批改数据）
 * @date 2019-07-0921:01
 */
@RestController
@Slf4j
@RequestMapping("api/v3/goods")
public class ApiCorrectGoodsControllerV3 {

    @Autowired
    CorrectGoodsServiceV1 correctGoodsServiceV1;
    
    @Autowired
	CorrectGoodsService correctGoodsService;

    /**
     * 批改商品列表V2
     * @param userSession
     * @param page
     * @param pageSize
     * @return
     */
    @LogPrint
    @GetMapping(value="correctGoodsList",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object correctGoods(@Token(check = false) UserSession userSession,
                                                  @RequestParam(name = "page", defaultValue = "1") int page,
                                                  @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {
        return correctGoodsServiceV1.showCorrectGoods();

    }
    
    /**
	 * 切换支付宝帐号 依然实用老加密方法
	 * @param userSession
	 * @param orderInfo
	 * @param flag
	 * @return
	 */
	@LogPrint
	@PostMapping(value = "getAliPaySign", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public Object getAliPaySignV3(@Token(check = false) UserSession userSession, @RequestBody Map<String, String> orderInfo,
			@RequestParam(name = "flag", defaultValue = "0") Integer flag) {
		Assert.notNull(orderInfo, "签名字符串不能为空");
		return correctGoodsService.getAliPaySignV3(orderInfo.get("orderInfo"), flag);
	}

}
