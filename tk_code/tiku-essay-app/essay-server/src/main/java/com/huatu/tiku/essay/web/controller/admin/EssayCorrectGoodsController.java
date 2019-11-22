package com.huatu.tiku.essay.web.controller.admin;

import com.google.common.collect.Lists;
import com.huatu.tiku.essay.constant.status.EssayCorrectGoodsConstant;
import com.huatu.tiku.essay.entity.EssayCorrectGoods;
import com.huatu.tiku.essay.service.CorrectGoodsService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.vo.admin.AdminEssayCorrectGoodsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/end/goods")
/**
 * create by jbzm on 17/12/11
 */
public class EssayCorrectGoodsController {
    @Autowired
    CorrectGoodsService correctGoodsService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    /**
     * 查询商品列表
     * @param page
     * @param pageSize
     * @return
     */
    @LogPrint
    @GetMapping(value = "correctGoodsList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public PageUtil<EssayCorrectGoods> correctGoods(@RequestParam(name = "page", defaultValue = "1") int page,
                                                             @RequestParam(name = "pageSize", defaultValue = "20") int pageSize) {
        PageRequest pageRequest = new PageRequest(page - 1, pageSize, Sort.Direction.ASC, "price");
        List<EssayCorrectGoods> essayCorrectGoodsList = correctGoodsService.findByStatus(pageRequest);
        long total = correctGoodsService.countByStatus(EssayCorrectGoodsConstant.CorrectGoodsStatusEnum.NORMAL.getStatus());

        PageUtil p = PageUtil.builder()
                .result(essayCorrectGoodsList)
                .next(total > page * pageSize ? 1 : 0)
                .total(total)
                .totalPage((0 == total % pageSize)?(total / pageSize):(total / pageSize + 1))
                .build();
        return p;
    }

    /**
     * 新增商品
     * @param essayCorrectGoods
     * @return
     */
    @LogPrint
    @PostMapping(value = "correctGoods", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public EssayCorrectGoods insertGood(@RequestBody EssayCorrectGoods essayCorrectGoods) {
        String uid = "";
        if(essayCorrectGoods.getPrice() <= 0){
            essayCorrectGoods.setPrice(essayCorrectGoods.getActivityPrice());
        }
        if(null == essayCorrectGoods.getIsLimitNum()){
            essayCorrectGoods.setIsLimitNum(1);     //1为限定次数，为默认值
        }
        essayCorrectGoods.setInventory(Integer.MAX_VALUE);
        essayCorrectGoods.setBizStatus(EssayCorrectGoodsConstant.CorrectGoodsBizStatusEnum.OFFLINE.getBizStatus());
        essayCorrectGoods.setStatus(EssayCorrectGoodsConstant.CorrectGoodsStatusEnum.NORMAL.getStatus());
        essayCorrectGoods.setCreator(uid);
        essayCorrectGoods.setGmtCreate(new Date());
        return correctGoodsService.saveGoods(essayCorrectGoods);
    }

    /**
     * 修改商品状态
     * @return
     */
    @LogPrint
    @PutMapping(value = "status", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public int deleteGood(@RequestParam int type, @RequestParam long goodsId) {
        String uid = "";
        return correctGoodsService.modifyGoods(type,goodsId,uid);
    }

    /**
     * 修改商品
     * @param essayCorrectGoods
     * @return
     */
    @LogPrint
    @PutMapping(value = "correctGoods", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public EssayCorrectGoods updateGood(@RequestBody EssayCorrectGoods essayCorrectGoods){
        String uid = "";
        essayCorrectGoods.setInventory(Integer.MAX_VALUE);
        //修改商品可以让商品恢复到初始状态
        essayCorrectGoods.setBizStatus(EssayCorrectGoodsConstant.CorrectGoodsBizStatusEnum.OFFLINE.getBizStatus());
        essayCorrectGoods.setStatus(EssayCorrectGoodsConstant.CorrectGoodsStatusEnum.NORMAL.getStatus());
        essayCorrectGoods.setModifier(uid);
        essayCorrectGoods.setGmtModify(new Date());
        return correctGoodsService.saveGoods(essayCorrectGoods);
    }

    /**
     * PHP获取赠品列表
     *
     * @param type 批改类型
     * @return 商品列表
     */
    @GetMapping(value = "correctGoodsGiftList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<AdminEssayCorrectGoodsVO> correctGoodsGiftList(Integer type) {
        List<EssayCorrectGoods> correctGoods = correctGoodsService.correctGoodsGiftList(type);

        List<AdminEssayCorrectGoodsVO> correctGoodsVOS = Lists.newArrayListWithExpectedSize(correctGoods.size());

        correctGoods.forEach(correctGood -> {
            AdminEssayCorrectGoodsVO correctGoodsVO = new AdminEssayCorrectGoodsVO();

            BeanUtils.copyProperties(correctGood, correctGoodsVO);

            correctGoodsVOS.add(correctGoodsVO);
        });

        return correctGoodsVOS;
    }
}
