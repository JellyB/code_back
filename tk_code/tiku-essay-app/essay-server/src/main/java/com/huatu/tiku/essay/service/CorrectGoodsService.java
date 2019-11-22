package com.huatu.tiku.essay.service;

import com.huatu.tiku.essay.entity.EssayCorrectGoods;
import com.huatu.tiku.essay.vo.resp.EssayCorrectGoodsVO;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CorrectGoodsService {

    List<EssayCorrectGoodsVO> list(Pageable pageable);

    List<EssayCorrectGoods> findByStatus(PageRequest pageRequest);

    long countByStatus(int status);

    EssayCorrectGoods saveGoods(EssayCorrectGoods essayCorrectGoods);

    int modifyGoods(int type, long goodsId, String uid);

    List<EssayCorrectGoodsVO> listV2(Pageable pageable);

    /**
     * 获取支付宝签名信息
     * @param orderInfo
     * @return
     */
	String getAliPaySign(String orderInfo,Integer flag);

    /**
     * 根据ID查询
     *
     * @param id 商品ID
     * @return 商品信息
     */
    EssayCorrectGoods getById(Long id);

    /**
     * 赠送课程列表
     *
     * @param type 批改类型
     * @return 商品列表
     */
    List<EssayCorrectGoods> correctGoodsGiftList(Integer type);

    /**
     * 支付宝签名v2
     * @param orderInfo
     * @param flag
     * @return
     */
	String getAliPaySignV2(String orderInfo, Integer flag);
	
	/**
	 * 支付宝签名v3 切换支付宝帐号
	 * @param orderInfo
	 * @param flag
	 * @return
	 */
	String getAliPaySignV3(String orderInfo, Integer flag);
}
