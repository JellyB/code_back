package com.huatu.tiku.essay.service.impl;

import com.google.common.collect.Lists;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.constant.status.EssayCorrectGoodsConstant;
import com.huatu.tiku.essay.entity.EssayCorrectGoods;
import com.huatu.tiku.essay.essayEnum.EssayCorrectGoodsSaleTypeEnum;
import com.huatu.tiku.essay.essayEnum.EssayStatusEnum;
import com.huatu.tiku.essay.repository.EssayCorrectGoodsRepository;
import com.huatu.tiku.essay.service.CorrectGoodsService;
import com.huatu.tiku.essay.util.Result;
import com.huatu.tiku.essay.util.file.HtmlFileUtil;
import com.huatu.tiku.essay.util.pay.AliPayUtil;
import com.huatu.tiku.essay.vo.resp.EssayCorrectGoodsVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.persistence.criteria.Predicate;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

//@Transactional
@Service
@Slf4j
public class CorrectGoodsServiceImpl implements CorrectGoodsService {

    @Autowired
    EssayCorrectGoodsRepository essayCorrectGoodsRepository;
    @Autowired
    HtmlFileUtil htmlFileUtil;

    @Value("${goods_binding_course}")
    private String goodsBindingCourseUrl;

    @Autowired
    RestTemplate restTemplate;


    @Override
    public List<EssayCorrectGoodsVO> list(Pageable pageable) {
        List<EssayCorrectGoods> correctGoodsList = essayCorrectGoodsRepository.findByBizStatusAndStatusAndSaleTypeAndInventoryGreaterThan(pageable, EssayCorrectGoodsConstant.CorrectGoodsBizStatusEnum.SELLING.getBizStatus(), EssayCorrectGoodsConstant.CorrectGoodsStatusEnum.NORMAL.getStatus(), EssayCorrectGoodsSaleTypeEnum.APP_SALE, 0);
        correctGoodsList.removeIf(i -> i.getCorrectMode() != 1);
        List<EssayCorrectGoodsVO> correctGoodsVOList = new LinkedList<EssayCorrectGoodsVO>();
        for (EssayCorrectGoods correctGoods : correctGoodsList) {
            EssayCorrectGoodsVO vo = new EssayCorrectGoodsVO();
            BeanUtils.copyProperties(correctGoods, vo);
            vo.setPrice(correctGoods.getActivityPrice());
            vo.setDoublePrice(vo.getPrice() / (double) 100);

            correctGoodsVOList.add(vo);
        }
        return correctGoodsVOList;
    }


    /**
     * @param pageRequest 分页
     * @return
     */
    @Override
    public List<EssayCorrectGoods> findByStatus(PageRequest pageRequest) {

        List<EssayCorrectGoods> goodsList = essayCorrectGoodsRepository.findByStatusOrderByIdDesc(EssayCorrectGoodsConstant.CorrectGoodsStatusEnum.NORMAL.getStatus());
        return goodsList;
    }


    @Override
    public long countByStatus(int status) {
        return essayCorrectGoodsRepository.countByStatus(status);
    }

    @Override
    public EssayCorrectGoods saveGoods(EssayCorrectGoods essayCorrectGoods) {
        if (essayCorrectGoods.getId() <= 0) {
            essayCorrectGoods.setBizStatus(EssayCorrectGoodsConstant.CorrectGoodsBizStatusEnum.OFFLINE.getBizStatus());
            essayCorrectGoods.setStatus(EssayCorrectGoodsConstant.CorrectGoodsStatusEnum.NORMAL.getStatus());
        }
        return essayCorrectGoodsRepository.save(essayCorrectGoods);
    }

    @Override
    public int modifyGoods(int type, long goodsId, String uid) {

        int status = 1;
        int bizStatus = 0;
        EssayCorrectGoods essayCorrectGoods = essayCorrectGoodsRepository.findByIdAndStatus(goodsId, EssayStatusEnum.NORMAL.getCode());
        if (null == essayCorrectGoods) {
            throw new BizException(EssayErrors.GOODS_NOT_EXIT);
        }
        if (EssayCorrectGoodsConstant.ON_LINE == type) {
            status = EssayCorrectGoodsConstant.CorrectGoodsStatusEnum.NORMAL.getStatus();
            bizStatus = EssayCorrectGoodsConstant.CorrectGoodsBizStatusEnum.SELLING.getBizStatus();
        } else if (EssayCorrectGoodsConstant.OFF_LINE == type) {
            //商品下线
            if (essayCorrectGoods.getSaleType() == EssayCorrectGoodsSaleTypeEnum.COURSE_GIFT) {
                checkIsBindCourse(goodsId);
            }
            status = EssayCorrectGoodsConstant.CorrectGoodsStatusEnum.NORMAL.getStatus();
            bizStatus = EssayCorrectGoodsConstant.CorrectGoodsBizStatusEnum.OFFLINE.getBizStatus();
        } else if (EssayCorrectGoodsConstant.DELETE == type) {
            //商品下线
            if (essayCorrectGoods.getSaleType() == EssayCorrectGoodsSaleTypeEnum.COURSE_GIFT) {
                checkIsBindCourse(goodsId);
            }
            status = EssayCorrectGoodsConstant.CorrectGoodsStatusEnum.DELETED.getStatus();
        } else {
            log.info("参数异常。操作类型错误 {}" + type);
            throw new BizException(EssayErrors.GOODS_SAVE_TYPE_ERROR);
        }
        int count = essayCorrectGoodsRepository.modifyCorrectGoodsBizStatusAndStatusById(bizStatus, status, uid, new Date(), goodsId);

        return count;
    }

    /*
     *  查询批改商品V2
     */
    @Override
    public List<EssayCorrectGoodsVO> listV2(Pageable pageable) {
        //查询可用状态的商品
        List<EssayCorrectGoods> correctGoodsList = essayCorrectGoodsRepository.findByBizStatusAndStatusAndSaleTypeAndInventoryGreaterThan(pageable, EssayCorrectGoodsConstant.CorrectGoodsBizStatusEnum.SELLING.getBizStatus(), EssayCorrectGoodsConstant.CorrectGoodsStatusEnum.NORMAL.getStatus(), EssayCorrectGoodsSaleTypeEnum.APP_SALE, 0);
        correctGoodsList.removeIf(i -> i.getCorrectMode() != 1);
        List<EssayCorrectGoodsVO> correctGoodsVOList = new LinkedList<EssayCorrectGoodsVO>();
        for (EssayCorrectGoods correctGoods : correctGoodsList) {

            EssayCorrectGoodsVO vo = new EssayCorrectGoodsVO();
            BeanUtils.copyProperties(correctGoods, vo);
            if (vo.getActivityPrice() == vo.getPrice()) {
                vo.setPrice(0);
            }
            vo.setDoublePrice(vo.getPrice() / (double) 100);
            vo.setDoubleActivityPrice(vo.getActivityPrice() / (double) 100);
            correctGoodsVOList.add(vo);
        }
        return correctGoodsVOList;
    }

    /**
     * 获取支付保签名信息
     */
    @Override
    public String getAliPaySign(String orderInfo, Integer flag) {
        log.info("签名字符串为:{},flag为:{}", orderInfo, flag);
        String sign = AliPayUtil.sign(orderInfo, flag);
        log.info("签名为:{}", sign);
        return sign;
    }

    @Override
    public EssayCorrectGoods getById(Long id) {
        return essayCorrectGoodsRepository.findOne(id);
    }

    @Override
    public List<EssayCorrectGoods> correctGoodsGiftList(Integer type) {
        return essayCorrectGoodsRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = Lists.newArrayList();

            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }

            predicates.add(cb.equal(root.get("saleType"), EssayCorrectGoodsSaleTypeEnum.COURSE_GIFT.ordinal()));
            predicates.add(cb.equal(root.get("bizStatus"), EssayCorrectGoodsConstant.CorrectGoodsBizStatusEnum.SELLING.getBizStatus()));

            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        });
    }


    @Override
    public String getAliPaySignV2(String orderInfo, Integer flag) {
        log.info("签名字符串为:{},flag为:{}", orderInfo, flag);
        String sign = AliPayUtil.signV2(orderInfo, flag);
        log.info("签名为:{}", sign);
        return sign;
    }
    
    @Override
	public String getAliPaySignV3(String orderInfo, Integer flag) {
        log.info("v3签名字符串为:{},flag为:{}", orderInfo, flag);
        String sign = AliPayUtil.signV3(orderInfo, flag);
        log.info("签名为:{}", sign);
        return sign;
    }

    //调用php接口,校验是否有删除内容
    private void checkIsBindCourse(Long goodId) {
        String url = goodsBindingCourseUrl + "?goodsId=" + goodId;
        Result result = restTemplate.getForObject(url, Result.class);
        if (null != result) {
            List<Integer> dataList = (List<Integer>) result.getData();
            if (CollectionUtils.isNotEmpty(dataList)) {
                String courseIdList = dataList.stream().map(id -> String.valueOf(id)).collect(Collectors.joining(","));
                String message = String.format("此商品绑定着在售（已上线）课程,不可下线或删除!,课程ID包含: %s", courseIdList);
                throw new BizException(ErrorResult.create(1000610, message));
            }
        }
    }
}
