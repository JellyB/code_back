package com.huatu.tiku.essay.web.controller.admin.v2;

import com.huatu.tiku.essay.constant.status.EssayCorrectGoodsConstant;
import com.huatu.tiku.essay.dto.EssayCorrectGoodsDto;
import com.huatu.tiku.essay.entity.EssayCorrectGoods;
import com.huatu.tiku.essay.essayEnum.CorrectModeEnum;
import com.huatu.tiku.essay.service.correct.CorrectGoodsServiceV2;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.vo.resp.EssayCorrectGoodsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@Slf4j
@RequestMapping("/end/v2/goods")
public class EssayCorrectGoodsControllerV2 {


    @Autowired
    CorrectGoodsServiceV2 correctGoodsServiceV2;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    /**
     * 新增商品
     * @param essayCorrectGoods
     * @return
     */
    @LogPrint
    @PostMapping(value = "correctGoods", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public EssayCorrectGoods insertGood(@RequestBody EssayCorrectGoodsDto essayCorrectGoods) {
        return correctGoodsServiceV2.saveGoods(essayCorrectGoods);
    }

    /**
     * 修改商品
     * @param essayCorrectGoods
     * @return
     */
    @LogPrint
    @PutMapping(value = "correctGoods", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public EssayCorrectGoods updateGood(@RequestBody EssayCorrectGoodsDto essayCorrectGoods){
        return correctGoodsServiceV2.saveGoods(essayCorrectGoods);
    }


    /**
     * 查询商品列表
     * @param page
     * @param pageSize
     * @return
     */
    @LogPrint
    @GetMapping(value = "correctGoodsList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public PageUtil<EssayCorrectGoodsVO> correctGoods(
                                            @RequestParam(value = "desc", defaultValue = "") String desc,
                                            @RequestParam(value = "type", defaultValue = "-1") int type,
                                            @RequestParam(value = "correctMode", defaultValue = "0") int correctMode,
                                            Integer saleType,
                                            @RequestParam(name = "page", defaultValue = "1") int page,
                                            @RequestParam(name = "pageSize", defaultValue = "20") int pageSize) {
        List<EssayCorrectGoodsConstant.GoodsTypeEnum> goodsTypeEnums = EssayCorrectGoodsConstant.GoodsTypeEnum.createDefaultNull(type, correctMode);
        CorrectModeEnum correctModeEnum = CorrectModeEnum.createDefaultNull(correctMode);
        return correctGoodsServiceV2.list(desc, goodsTypeEnums, correctModeEnum, saleType, page, pageSize);
    }



    /**
     * 修改商品状态
     * @return
     */
    @LogPrint
    @PutMapping(value = "status", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object deleteGood(@RequestParam int type, @RequestParam long goodsId) {
        String uid = "";
        return correctGoodsServiceV2.modifyGoods(type,goodsId,uid);
    }
    
    /**
     * 根据商品id查询商品上下线状态（课程上线时需要做校验）
     * @return
     */
    @PostMapping(value = "findStatusByIds", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<EssayCorrectGoodsVO> findStatusByIds(@RequestBody List<Long> ids){
    	return correctGoodsServiceV2.findStatusByIds(ids);
    }
}
