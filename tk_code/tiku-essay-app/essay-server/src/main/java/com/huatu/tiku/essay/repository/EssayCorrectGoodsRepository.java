package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.EssayCorrectGoods;
import com.huatu.tiku.essay.essayEnum.EssayCorrectGoodsSaleTypeEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface EssayCorrectGoodsRepository extends JpaRepository<EssayCorrectGoods, Long>, JpaSpecificationExecutor<EssayCorrectGoods> {
    List<EssayCorrectGoods> findByBizStatusAndStatusAndSaleTypeAndInventoryGreaterThan(Pageable pageable, int bizStatus, int status, EssayCorrectGoodsSaleTypeEnum saleType, int inventory);

    List<EssayCorrectGoods> findByBizStatusAndStatusAndSaleTypeAndInventoryGreaterThan(int bizStatus, int status, EssayCorrectGoodsSaleTypeEnum saleType, int inventory);

    List<EssayCorrectGoods> findByIdIn(List<Long> goodsIds);

    @Transactional
    @Modifying
    @Query("update EssayCorrectGoods cg set cg.inventory=?1  where cg.id=?2")
    int modifyCorrectGoodsInventoryById(int inventory, long id);

    @Transactional
    @Modifying
    @Query("update EssayCorrectGoods cg set cg.status=?1  where cg.id=?2")
    int modifyCorrectGoodsStatusById(int status, long id);

    @Transactional
    @Modifying
    @Query("update EssayCorrectGoods cg set cg.bizStatus=?1 ,status = ?2 ,cg.creator=?3 ,cg.gmtCreate=?4 where cg.id=?5")
    int modifyCorrectGoodsBizStatusAndStatusById(int bizStatus, int status, String uid, Date gmtCreate, long id);


    /* 商品修改库存*/
    @Transactional
    @Modifying
    @Query("update EssayCorrectGoods cg set cg.inventory = cg.inventory-?1,cg.salesNum = cg.salesNum + ?1 where cg.id = ?2 and cg.inventory >= ?1")
    int modifyInventoryAndSalesNumById(int num, long id);

    long countByIdAndBizStatusAndStatusAndInventoryGreaterThan(long goodsId, int bizStatus, int status, int count);


    long countByIdAndBizStatusAndStatusAndInventoryGreaterThanEqual(long goodsId, int bizStatus, int status, int count);


    List<EssayCorrectGoods> findByStatusOrderByIdDesc(int status);

    long countByStatus(int status);

    Page<EssayCorrectGoods> findByNameIsLikeAndTypeAndCorrectMode(String name, int type, int correctMode, Pageable pageable);

    EssayCorrectGoods findByIdAndStatus(long id, int status);

}

