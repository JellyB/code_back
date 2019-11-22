package com.huatu.tiku.essay.service.correct;

import com.huatu.tiku.essay.vo.resp.EssayCorrectGoodsVO;
import org.springframework.data.domain.PageRequest;

import java.util.List;

/**
 * @author huangqingpeng
 * @title: CorrectGoodsServiceV1
 * @description: 批改商品相关时限
 * @date 2019-07-0921:04
 */
public interface CorrectGoodsServiceV1 {
    /**
     * 查询需要展示的批改商品
     * @return
     */
    Object showCorrectGoods();

}
