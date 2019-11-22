package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.EssayGoodsOrder;
import com.huatu.tiku.essay.entity.EssayGoodsOrderDetail;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by x6 on 2017/11/28.
 */
public interface EssayGoodsOrderDetailRepository extends JpaRepository<EssayGoodsOrderDetail, Long> {
    List<EssayGoodsOrderDetail> findByRecordId(long recordId);

    /**
     * 查询用户所有有时限，状态正常且剩余次数大于0的订单详情
     * @param userId
     * @param isLimitNum
     * @param status
     * @param num
     * @return
     */
    List<EssayGoodsOrderDetail> findByUserIdAndIsLimitNumAndStatusAndNumGreaterThan(long userId,int isLimitNum,int status,int num);

    /**
     * 查询特殊适用范围的订单详情
     * @param userId
     * @param goodsType
     * @param status
     * @param specialId
     * @return
     */
    List<EssayGoodsOrderDetail> findByUserIdAndGoodsTypeAndStatusAndSpecialId(long userId,int goodsType,int status,int specialId);

    /**
     * 分页查询用户某类批改商品的订单详情
     * @param pageable
     * @param userId
     * @param goodsType
     * @param status
     * @return
     */
    List<EssayGoodsOrderDetail> findByUserIdAndGoodsTypeAndStatus(Pageable pageable, long userId, int goodsType, int status);

    long countByUserIdAndGoodsTypeAndStatus(long userId, int type, int status);

    /**
     * 用户某个批改类型相关的详情
     * @param userId
     * @param goodsType
     * @return
     */
    List<EssayGoodsOrderDetail> findByUserIdAndGoodsType(long userId, int goodsType);
    
    /**
     * 查询用户指定类型的剩余批改次数的订单详情
     * @param userId
     * @param isLimitNum
     * @param status
     * @param num
     * @return
     */
    List<EssayGoodsOrderDetail> findByUserIdAndIsLimitNumAndStatusAndGoodsTypeAndNumGreaterThanOrderByExpireDate(long userId,int isLimitNum,int status,int goodsType,int num);


    List<EssayGoodsOrderDetail> findByStatus( int status);

}
