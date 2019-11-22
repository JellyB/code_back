package com.huatu.tiku.essay.service.impl;

import com.huatu.tiku.essay.constant.status.EssayGoodsOrderConstant;
import com.huatu.tiku.essay.entity.EssayGoodsOrder;
import com.huatu.tiku.essay.repository.EssayGoodsOrderRepository;
import com.huatu.tiku.essay.service.EssayGalaxyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by x6 on 2018/3/20.
 */
@Service
@Slf4j
public class EssayGalaxyServiceImpl  implements EssayGalaxyService {

    @Autowired
    EssayGoodsOrderRepository essayGoodsOrderRepository;

    @Override
    public Object order(Long start, Long end) {
        //处理开始结束时间
        Date startDate = new Date();
        Date endDate = new Date();
        if(null == start || 0 == start ){
            start = 0L;
        }
        if(null == end || null == end){
            end = System.currentTimeMillis()/1000;
        }

        startDate = new Date(start*1000L);
        endDate = new Date(end*1000L);
        List<EssayGoodsOrder> orderList = essayGoodsOrderRepository.findByBizStatusAndStatusAndGmtModifyBetween
                (EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.PAYED.getBizStatus(), EssayGoodsOrderConstant.EssayGoodsOrderStatusEnum.NORMAL.getStatus(), startDate, endDate);

        long count = essayGoodsOrderRepository.countByBizStatusAndStatusAndGmtModifyBetween
                ( EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.PAYED.getBizStatus(), EssayGoodsOrderConstant.EssayGoodsOrderStatusEnum.NORMAL.getStatus(), startDate, endDate);


//        PageUtil p = PageUtil.builder()
//                .result(orderList)
//                .total(count)
//                .totalPage((0 == count % pageRequest.getPageSize()) ? (count / pageRequest.getPageSize()) : ((count / pageRequest.getPageSize()) + 1))
//                .build();
        return orderList;
    }
}
