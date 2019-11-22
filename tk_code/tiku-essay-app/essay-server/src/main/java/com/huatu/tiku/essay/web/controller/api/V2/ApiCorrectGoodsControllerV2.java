package com.huatu.tiku.essay.web.controller.api.V2;

import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.vo.resp.EssayCorrectGoodsVO;
import com.huatu.tiku.essay.service.CorrectGoodsService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.springboot.users.support.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("api/v2/goods")
/**
 * 批改商品
 */

public class ApiCorrectGoodsControllerV2 {
    @Autowired
    CorrectGoodsService correctGoodsService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    /**
     * 批改商品列表V2
     * @param userSession
     * @param page
     * @param pageSize
     * @return
     */
    @LogPrint
    @GetMapping(value="correctGoodsList",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<EssayCorrectGoodsVO> correctGoods(@Token (check = false) UserSession userSession,
                                                  @RequestParam(name = "page", defaultValue = "1") int page,
                                                  @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {
        PageRequest pageRequest = new PageRequest(page - 1, pageSize, Sort.Direction.ASC, "type");
        List<EssayCorrectGoodsVO> essayCorrectGoodsList = correctGoodsService.listV2(pageRequest);
        return essayCorrectGoodsList;
    }


}
