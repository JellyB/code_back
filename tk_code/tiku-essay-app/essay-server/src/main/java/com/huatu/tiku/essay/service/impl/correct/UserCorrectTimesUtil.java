package com.huatu.tiku.essay.service.impl.correct;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.huatu.tiku.essay.constant.status.EssayCorrectGoodsConstant;
import com.huatu.tiku.essay.entity.EssayGoodsOrderDetail;
import com.huatu.tiku.essay.entity.EssayUserCorrectGoods;
import com.huatu.tiku.essay.vo.resp.correct.CorrectTimesSimpleVO;
import com.huatu.tiku.essay.vo.resp.correct.UserCorrectTimesVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.core.util.JsonUtils;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author huangqingpeng
 * @title: UserCorrectTimesUtil
 * @description: 用户修改次数工具
 * @date 2019-07-0910:49
 */
@Slf4j
public class UserCorrectTimesUtil {

    private final static Integer WILL_EXPIRE_DAYS = 10;

    /**
     * 组装用户批改次数
     */
    public static BiFunction<List<EssayUserCorrectGoods>, List<EssayGoodsOrderDetail>, UserCorrectTimesVO> assemblingUserCorrectTimes =
            ((essayUserCorrectGoodsList,        //总批改次数统计记录
              orderDetails)                     //有时限的订单详情记录
                    -> {
                List<CorrectTimesSimpleVO> machineCorrect = Lists.newArrayList();
                List<CorrectTimesSimpleVO> manualCorrect = Lists.newArrayList();

                for (EssayCorrectGoodsConstant.GoodsTypeEnum goodsTypeEnum : EssayCorrectGoodsConstant.GoodsTypeEnum.values()) {
                    int type = goodsTypeEnum.getType();
                    CorrectTimesSimpleVO correctTimesSimpleVO = initCorrectTimesVO(goodsTypeEnum);
                    if (CollectionUtils.isNotEmpty(essayUserCorrectGoodsList)) {
                        List<EssayUserCorrectGoods> correctGoodsList = essayUserCorrectGoodsList.stream()
                                .filter(i -> i.getType() == type).collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(correctGoodsList)) {
                            EssayUserCorrectGoods userCorrectGoods = correctGoodsList.get(0);
                            int usefulNum = userCorrectGoods.getUsefulNum();
                            log.info("assemblingUserCorrectTimes:userId:{},goodType:{},usefulNum:{},correctGoodsList={}",
                                    userCorrectGoods.getUserId(), goodsTypeEnum.getName(), userCorrectGoods.getUsefulNum(),
                                    new Gson().toJson(correctGoodsList));
                            correctTimesSimpleVO.setNum(usefulNum);
                        }
                    }
                    Date now = new Date();
                    if (CollectionUtils.isNotEmpty(orderDetails)) {
                        long count = orderDetails.stream().filter(i -> i.getGoodsType() == type)
                                .filter(i -> i.getIsLimitNum() == 1)
                                .filter(i -> i.getExpireFlag() == 1)
                                .filter(i -> i.getExpireDate().after(now))
                                .filter(i -> i.getExpireDate().before(new Date(now.getTime() + TimeUnit.DAYS.toMillis(WILL_EXPIRE_DAYS))))
                                .mapToInt(i -> i.getNum()).sum();
                        correctTimesSimpleVO.setWillExpireNum(new Long(count).intValue());
                    }
                    switch (goodsTypeEnum.getCorrectTypeEnum()) {
                        case MANUAL:
                            manualCorrect.add(correctTimesSimpleVO);
                            break;
                        case INTELLIGENCE:
                            machineCorrect.add(correctTimesSimpleVO);
                            break;
                    }
                }
                UserCorrectTimesVO userCorrectTimesVO = new UserCorrectTimesVO();
                userCorrectTimesVO.setMachineCorrect(machineCorrect);
                userCorrectTimesVO.setManualCorrect(manualCorrect);
                int machineNum = machineCorrect.stream().mapToInt(i -> i.getNum()).sum();
                int manualNum = manualCorrect.stream().mapToInt(i -> i.getNum()).sum();
                userCorrectTimesVO.setTotalNum(machineNum + manualNum);
                return userCorrectTimesVO;
            });

    public static CorrectTimesSimpleVO initCorrectTimesVO(EssayCorrectGoodsConstant.GoodsTypeEnum goodsTypeEnum) {
        CorrectTimesSimpleVO initVO = CorrectTimesSimpleVO.builder()
                .goodsName(goodsTypeEnum.getName())
                .goodsType(goodsTypeEnum.getType())
                .isLimitNum(1)
                .num(0)
                .willExpireNum(0)
                .specialNum(0).build();
        return initVO;
    }
}
