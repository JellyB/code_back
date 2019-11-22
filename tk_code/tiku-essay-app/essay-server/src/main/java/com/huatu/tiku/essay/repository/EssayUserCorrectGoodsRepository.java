package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.EssayUserCorrectGoods;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface EssayUserCorrectGoodsRepository extends JpaRepository<EssayUserCorrectGoods, Long> {
    List<EssayUserCorrectGoods> findByUserIdAndBizStatusAndStatus(int userId, int bizStatus, int status);

    List<EssayUserCorrectGoods> findByUserIdAndBizStatusAndStatusAndType(int userId, int bizStatus, int status, int type);
    @Transactional
    @Modifying
    @Query("update EssayUserCorrectGoods ucg set ucg.usefulNum=ucg.usefulNum+?1,ucg.totalNum=ucg.totalNum+?1 where ucg.userId=?2 and ucg.type = ?3 and status = 1")
    int modifyUsefulNumAndTotalNumByUserIdAndType(int num, int userId, int type);
    @Transactional
    @Modifying
    @Query("update EssayUserCorrectGoods ucg set ucg.usefulNum=ucg.usefulNum-?1 where ucg.userId=?2 and ucg.type = ?3 and ucg.usefulNum >= 1 and status = 1")
    int modifyUsefulNumByUserIdAndType(int count, int userId, int type);

    long countByUserIdAndTypeAndStatusAndBizStatus(int userId, int type, int status, int bizStatus);

    @Transactional
    @Modifying
    @Query("update EssayUserCorrectGoods ucg set ucg.totalNum=ucg.totalNum+?3,ucg.usefulNum=ucg.usefulNum+?4,ucg.specialNum=ucg.specialNum+?5,ucg.isLimitNum=?6,ucg.expireTime=?7 where ucg.userId=?1 and ucg.type = ?2 and status = 1")
    int modifyByUserIdAndGoodsType(int userId, int type, int totalNum, int usefulNum, int specialNum, int isLimitNum, Date expireTime);

	EssayUserCorrectGoods findByUserIdAndStatusAndType(int userId, int status, int singleQuestion);

    List<EssayUserCorrectGoods> findByStatusAndIsLimitNum(int userId, int isLimitNum);
}

