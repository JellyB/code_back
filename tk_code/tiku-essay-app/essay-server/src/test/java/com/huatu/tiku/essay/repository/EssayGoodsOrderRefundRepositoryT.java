package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.EssayGoodsOrderRefund;
import com.huatu.tiku.essay.test.correct.TikuBaseTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Slf4j
public class EssayGoodsOrderRefundRepositoryT extends TikuBaseTest {

    @Autowired
    private EssayGoodsOrderRefundRepository essayGoodsOrderRefundRepository;

    @Test
    public void findByGoodsOrderId() {
        List<EssayGoodsOrderRefund> goodsOrderRefunds = essayGoodsOrderRefundRepository.findByGoodsOrderId(2479L);

        log.info("Result is {}", goodsOrderRefunds);
    }

    @Test
    public void findTop1ByGoodsOrderIdOrderByIdDesc() {
        EssayGoodsOrderRefund goodsOrderRefund = essayGoodsOrderRefundRepository.findTop1ByGoodsOrderIdOrderByIdDesc(2479L);

        log.info("Result is {}", goodsOrderRefund);
    }
}
