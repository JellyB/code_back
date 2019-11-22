package com.huatu.tiku.essay.service.impl.correct;

import com.google.common.collect.Lists;
import com.huatu.common.SuccessMessage;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.constant.status.EssayCorrectGoodsConstant;
import com.huatu.tiku.essay.dto.EssayCorrectGoodsDto;
import com.huatu.tiku.essay.entity.EssayCorrectGoods;
import com.huatu.tiku.essay.essayEnum.CorrectModeEnum;
import com.huatu.tiku.essay.essayEnum.EssayCorrectGoodsSaleTypeEnum;
import com.huatu.tiku.essay.essayEnum.EssayStatusEnum;
import com.huatu.tiku.essay.repository.EssayCorrectGoodsRepository;
import com.huatu.tiku.essay.service.correct.CorrectGoodsServiceV2;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.util.Result;
import com.huatu.tiku.essay.vo.resp.EssayCorrectGoodsVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.persistence.criteria.Predicate;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class CorrectGoodsServiceImplV2 implements CorrectGoodsServiceV2 {

    @Autowired
    EssayCorrectGoodsRepository essayCorrectGoodsRepository;

    /**
     * 新增商品
     *
     * @param essayCorrectGoods
     * @return
     */
    @Override
    public EssayCorrectGoods saveGoods(EssayCorrectGoodsDto essayCorrectGoods) {
        String uid = "";
        if (essayCorrectGoods.getPrice() <= 0) {
            essayCorrectGoods.setPrice(essayCorrectGoods.getActivityPrice());
        }
        essayCorrectGoods.setInventory(Integer.MAX_VALUE);
        essayCorrectGoods.setCreator(uid);
        essayCorrectGoods.setGmtCreate(new Date());
        if (essayCorrectGoods.getId() <= 0) {
            essayCorrectGoods.setBizStatus(EssayCorrectGoodsConstant.CorrectGoodsBizStatusEnum.OFFLINE.getBizStatus());
            essayCorrectGoods.setStatus(EssayCorrectGoodsConstant.CorrectGoodsStatusEnum.NORMAL.getStatus());
        }
        if (essayCorrectGoods.getExpireFlag() == EssayCorrectGoodsConstant.GoodsExpireFlagEnum.LIMITED.getType()) {
            essayCorrectGoods.setExpireDate(essayCorrectGoods.getExpireDate());
        }

        EssayCorrectGoods essayCorrectGoods_ = new EssayCorrectGoods();
        BeanUtils.copyProperties(essayCorrectGoods, essayCorrectGoods_);
        essayCorrectGoods_.setSaleType(EssayCorrectGoodsSaleTypeEnum.of(essayCorrectGoods.getSaleType()));
        essayCorrectGoods_.setType(EssayCorrectGoodsConstant.GoodsTypeEnum.create(essayCorrectGoods.getType(), essayCorrectGoods.getCorrectMode()).getType());
        return essayCorrectGoodsRepository.save(essayCorrectGoods_);
    }


    @Override
    public PageUtil<EssayCorrectGoodsVO> list(String name, List<EssayCorrectGoodsConstant.GoodsTypeEnum> goodsTypeEnums, CorrectModeEnum correctModeEnum, Integer saleType, int page, int pageSize) {
        List<EssayCorrectGoodsVO> list = Lists.newArrayList();
        PageRequest pageRequest = new PageRequest(page - 1, pageSize, Sort.Direction.DESC, "gmtCreate");

        Specification<EssayCorrectGoods> specification = (root, query, cb) -> {
            List<Predicate> predicates = Lists.newArrayList();
            predicates.add(cb.equal(root.get("status"), EssayCorrectGoodsConstant.CorrectGoodsStatusEnum.NORMAL.getStatus()));
            if (StringUtils.isNotEmpty(name)) {
                predicates.add(cb.like(root.get("name"), "%" + name + "%"));
            }
            if (CollectionUtils.isNotEmpty(goodsTypeEnums)) {
                List<Integer> types = Lists.newArrayList();
                for (EssayCorrectGoodsConstant.GoodsTypeEnum goodsTypeEnum : goodsTypeEnums) {
                    if (null != goodsTypeEnum) {
                        types.add(goodsTypeEnum.getType());
                    }
                }
                if (CollectionUtils.isNotEmpty(types)) {
                    predicates.add(root.get("type").in(types));
                }
            }
            if (null != correctModeEnum) {
                predicates.add(cb.equal(root.get("correctMode"), correctModeEnum.getMode()));
            }
            if (null != saleType) {
                predicates.add(cb.equal(root.get("saleType"), saleType));
            }
            query.where(
                    cb.and(predicates.toArray(new javax.persistence.criteria.Predicate[predicates.size()])));
            return query.getRestriction();
        };

        Page<EssayCorrectGoods> page_ = essayCorrectGoodsRepository.findAll(specification, pageRequest);
        for (EssayCorrectGoods essayCorrectGoods : page_.getContent()) {
            EssayCorrectGoodsVO essayCorrectGoodsVO = new EssayCorrectGoodsVO();
            BeanUtils.copyProperties(essayCorrectGoods, essayCorrectGoodsVO);
            essayCorrectGoodsVO.setType(essayCorrectGoods.getType() % 3);
            if (essayCorrectGoods.getExpireFlag() == EssayCorrectGoodsConstant.GoodsExpireFlagEnum.LIMITED.getType()) {
                essayCorrectGoodsVO.setExpireDate(essayCorrectGoods.getExpireDate());
            }
            essayCorrectGoodsVO.setSaleType(essayCorrectGoods.getSaleType().ordinal());
            list.add(essayCorrectGoodsVO);
        }

        PageUtil p = PageUtil.builder()
                .result(list)
                .next(page_.getTotalPages() > page ? 1 : 0)
                .total(page_.getTotalElements())
                .totalPage(page_.getTotalPages())
                .build();
        return p;
    }


    @Override
    public Object modifyGoods(int type, long goodsId, String uid) {
        int status = 1;
        int bizStatus = 0;

        if (EssayCorrectGoodsConstant.ON_LINE == type) {
            //商品上线
            status = EssayCorrectGoodsConstant.CorrectGoodsStatusEnum.NORMAL.getStatus();
            bizStatus = EssayCorrectGoodsConstant.CorrectGoodsBizStatusEnum.SELLING.getBizStatus();
        } else if (EssayCorrectGoodsConstant.OFF_LINE == type) {
            status = EssayCorrectGoodsConstant.CorrectGoodsStatusEnum.NORMAL.getStatus();
            bizStatus = EssayCorrectGoodsConstant.CorrectGoodsBizStatusEnum.OFFLINE.getBizStatus();
        } else if (EssayCorrectGoodsConstant.DELETE == type) {

            status = EssayCorrectGoodsConstant.CorrectGoodsStatusEnum.DELETED.getStatus();
        } else {
            log.info("参数异常。操作类型错误 {}" + type);
            throw new BizException(EssayErrors.GOODS_SAVE_TYPE_ERROR);
        }
        essayCorrectGoodsRepository.modifyCorrectGoodsBizStatusAndStatusById(bizStatus, status, uid, new Date(), goodsId);
        return SuccessMessage.create("操作成功");
    }


    @Override
    public List<EssayCorrectGoodsVO> findStatusByIds(List<Long> ids) {
        List<EssayCorrectGoodsVO> ret = Lists.newArrayList();
        List<EssayCorrectGoods> goodsList = essayCorrectGoodsRepository.findByIdIn(ids);
        goodsList.forEach(goods -> {
            ret.add(EssayCorrectGoodsVO.builder().id(goods.getId()).bizStatus(goods.getBizStatus()).build());
        });
        return ret;
    }

}
