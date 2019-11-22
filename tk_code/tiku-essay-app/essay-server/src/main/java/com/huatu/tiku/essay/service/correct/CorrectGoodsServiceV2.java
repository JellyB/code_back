package com.huatu.tiku.essay.service.correct;

import com.huatu.tiku.essay.constant.status.EssayCorrectGoodsConstant;
import com.huatu.tiku.essay.dto.EssayCorrectGoodsDto;
import com.huatu.tiku.essay.entity.EssayCorrectGoods;
import com.huatu.tiku.essay.essayEnum.CorrectModeEnum;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.vo.resp.EssayCorrectGoodsVO;

import java.util.List;

public interface CorrectGoodsServiceV2 {

    /**
     * save
     * @param essayCorrectGoods
     * @return
     */
    EssayCorrectGoods saveGoods(EssayCorrectGoodsDto essayCorrectGoods);

    /**
     * update
     * @param type
     * @param goodsId
     * @param uid
     * @return
     */
    Object modifyGoods(int type, long goodsId, String uid);

    /**
     * select
     * @param name
     * @param goodsTypeEnums
     * @param correctModeEnum
     * @param saleType 售卖类型
     * @param page
     * @param pageSize
     * @return
     */
    PageUtil<EssayCorrectGoodsVO> list(String name, List<EssayCorrectGoodsConstant.GoodsTypeEnum> goodsTypeEnums, CorrectModeEnum correctModeEnum, Integer saleType, int page, int pageSize);

    /**
     * 
     * @param ids
     * @return
     */
	List<EssayCorrectGoodsVO> findStatusByIds(List<Long> ids);
}
