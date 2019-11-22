package com.huatu.tiku.essay.service.impl.correct;

import com.google.common.collect.Maps;
import com.huatu.tiku.essay.constant.status.EssayCorrectGoodsConstant;
import com.huatu.tiku.essay.entity.EssayCorrectGoods;
import com.huatu.tiku.essay.essayEnum.CorrectModeEnum;
import com.huatu.tiku.essay.essayEnum.EssayCorrectGoodsSaleTypeEnum;
import com.huatu.tiku.essay.repository.EssayCorrectGoodsRepository;
import com.huatu.tiku.essay.service.correct.CorrectGoodsServiceV1;
import com.huatu.tiku.essay.vo.resp.EssayCorrectGoodsVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author huangqingpeng
 * @title: CorrectGoodsServiceImplV1
 * @description: 商品相关时限
 * @date 2019-07-0921:04
 */
@Service
@Slf4j
public class CorrectGoodsServiceImplV1 implements CorrectGoodsServiceV1 {

    @Autowired
    private EssayCorrectGoodsRepository essayCorrectGoodsRepository;

    @Override
    public Object showCorrectGoods() {
        HashMap<String, List<EssayCorrectGoodsVO>> resultMap = Maps.newHashMap();

        List<EssayCorrectGoods> correctGoodsList = essayCorrectGoodsRepository.findByBizStatusAndStatusAndSaleTypeAndInventoryGreaterThan(
                EssayCorrectGoodsConstant.CorrectGoodsBizStatusEnum.SELLING.getBizStatus(),
                EssayCorrectGoodsConstant.CorrectGoodsStatusEnum.NORMAL.getStatus(),
                EssayCorrectGoodsSaleTypeEnum.APP_SALE,
                0);
            if(CollectionUtils.isEmpty(correctGoodsList)){
            return resultMap;
        }
        resultMap.put("machineCorrect",convertAndFilterGoods2VO.apply(correctGoodsList,CorrectModeEnum.INTELLIGENCE));
        resultMap.put("manualCorrect",convertAndFilterGoods2VO.apply(correctGoodsList,CorrectModeEnum.MANUAL));
        return resultMap;
    }

    private static BiFunction<List<EssayCorrectGoods>,CorrectModeEnum,List<EssayCorrectGoodsVO>> convertAndFilterGoods2VO = ((correctGoodsList,modeEnum) -> {
        Function<EssayCorrectGoods,EssayCorrectGoodsVO> convert = (essayCorrectGoods -> {
            EssayCorrectGoodsVO vo = new EssayCorrectGoodsVO();
            BeanUtils.copyProperties(essayCorrectGoods,vo);
            vo.setDoublePrice(vo.getPrice() / (double) 100);
            vo.setDoubleActivityPrice(vo.getActivityPrice() / (double) 100);
            vo.setIsLimitNum(essayCorrectGoods.getIsLimitNum());
            if(essayCorrectGoods.getExpireFlag() == 1 && essayCorrectGoods.getExpireDate() == 0 ){
                vo.setExpireFlag(0);
            }
            return vo;
        });

        return correctGoodsList.stream().filter(i -> i.getCorrectMode() == modeEnum.getMode())
                .map(convert::apply)
                .sorted(Comparator.comparing(EssayCorrectGoodsVO::getType))
                .collect(Collectors.toList());
    });
}
