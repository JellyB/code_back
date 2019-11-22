package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.EssayGoodsOrder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Created by x6 on 2017/11/28.
 */
public interface EssayGoodsOrderRepository extends JpaRepository<EssayGoodsOrder, Long>, JpaSpecificationExecutor<EssayGoodsOrder> {

    List<EssayGoodsOrder> findByUserIdAndBizStatusAndStatusAndGmtModifyGreaterThan
            (Pageable pageable, int userId, int bizStatus, int status, Date date);

    long countByUserIdAndBizStatusAndStatusAndGmtModifyGreaterThan( int userId, int bizStatus, int status, Date date);

    long countByIdAndPayTypeAndBizStatus(long id, int payType,int bizStatus);



    List<EssayGoodsOrder> findByBizStatusAndStatusAndGmtModifyBetween
            ( int bizStatus, int status, Date start, Date end );

    long countByBizStatusAndStatusAndGmtModifyBetween( int bizStatus, int status, Date startDate, Date endDate);


    /**
     * 根据订单状态分页查询订单
     * @param pageable
     * @param userId
     * @param bizStatus
     * @param status
     * @return
     */
    List<EssayGoodsOrder> findByUserIdAndBizStatusAndStatusOrderByGmtCreateDesc
            (Pageable pageable, int userId, int bizStatus, int status);


    @Transactional
    @Modifying
    @Query("update EssayGoodsOrder o set o.status= -1  ,o.gmtModify = current_timestamp where o.id = ?1")
    int upToDel(long id);


    @Transactional
    @Modifying
    @Query("update EssayGoodsOrder o set o.bizStatus = 4 ,o.gmtModify = current_timestamp where o.gmtCreate <= ?1 and o.bizStatus = 0 ")
    int updateOrderToClose(Date date);

    @Transactional
    @Modifying
    @Query("update EssayGoodsOrder o set o.bizStatus= 2 ,o.gmtModify = current_timestamp where o.id = ?1")
    int upToCancel(long id);

    /**
     * 通过ID批量查询订单信息
     * @param ids
     * @return
     */
    List<EssayGoodsOrder> findByIdIn(List<Long> ids);

    /**
     * 根据课程订单ID查询
     *
     * @param courseOrderId 课程订单ID
     * @return 订单列表
     */
    EssayGoodsOrder findOneByCourseOrderId(Long courseOrderId);


    List<EssayGoodsOrder> findByUserIdAndStatus(int userId, int bizStatus);
}
