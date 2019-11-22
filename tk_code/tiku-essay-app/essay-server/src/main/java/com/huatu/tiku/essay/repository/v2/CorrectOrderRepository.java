package com.huatu.tiku.essay.repository.v2;


import com.huatu.tiku.essay.entity.correct.CorrectOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/7/4
 * @描述 人工批改订单流转表
 */
public interface CorrectOrderRepository extends JpaRepository<CorrectOrder, Long>, JpaSpecificationExecutor<CorrectOrder> {

    CorrectOrder findByAnswerCardIdAndAnswerCardTypeAndStatus(long answerCardId, int answerCardType, int status);

    List<CorrectOrder> findByReceiveOrderTeacherEquals(long teacherId);

    /**
     * 计算老师某个类型订单的批改量
     *
     * @param teacherId
     * @param orderType
     * @param bizStatus
     * @return
     */
    long countByReceiveOrderTeacherAndTypeAndBizStatusIn(long teacherId, int orderType, List<Integer> bizStatus);

    List<CorrectOrder> findByReceiveOrderTeacherAndAnswerCardTypeAndBizStatus(long teacherId, int answerType, int bizStatus);

    CorrectOrder findByIdAndStatus(long id, int status);

    @Transactional
    @Modifying
    @Query("update CorrectOrder co set co.bizStatus=?4,co.receiveOrderTeacher=?2,co.receiveTime=?3,co.gmtModify=NOW() where  co.id= ?1 and co.status = 1")
    int updateBizStatusAndReceiveTimeById(long orderId, long teacherId, Date date, int bizStatus);

    @Transactional
    @Modifying
    @Query("update  CorrectOrder co set co.receiveOrderTeacher = ?1, co.bizStatus= ?3, co.gmtModify=NOW() where co.id= ?2 and co.status=1")
    int updateBizStatusByOrderId(long receiveOrderTeacher, long orderId, int bizStatus);

    @Transactional
    @Modifying
    @Query("update  CorrectOrder co set co.status = ?1  where co.answerCardId= ?2 and co.answerCardType=?3")
    int updateStatusByAnswerCardIdAndAnswerCardType(int status, long answerCardId, int answerCardType);

    //退回学员
    @Transactional
    @Modifying
    @Query("update  CorrectOrder  co set co.bizStatus=?2,co.correctMemo =?3, co.gmtModify=NOW() where  co.oldOrderId=?1 and co.status=1")
    int returnUser(long orderId, int bizStatus, String reason);

    //更新学员反馈状态
    @Transactional
    @Modifying
    @Query("update CorrectOrder  co set co.feedBackStatus=?3,co.bizStatus=?4, co.gmtModify=NOW() where  co.answerCardId =?1 and  co.answerCardType=?2 and co.status=1")
    int updateFeedBackStatus(long answerId, int answerType, int feedBackStatus, int bizStatus);

    /**
     * 获取老师薪资
     *
     * @param teacherId 老师ID
     * @param startDate 开始时间
     * @param endDate   结束时间
     */
    @Query(value = "SELECT type , count(*) FROM v_essay_correct_order o WHERE o.receive_order_teacher = ?1 AND end_time >= ?2 AND end_time <= ?3 AND o.biz_status in (4, 7) GROUP BY o.type ORDER BY o.type", nativeQuery = true)
    List<Object[]> getSalaryList(Long teacherId, Date startDate, Date endDate);

    /**
     * 查询某一状态的所有订单
     *
     * @param status
     * @param bizStatus
     * @return
     */
    List<CorrectOrder> findByStatusAndBizStatusOrderByGmtCreateAsc(int status, int bizStatus);

    /**
     * 查询用户相关订单
     *
     * @param userId
     * @param bizStatus
     * @return
     */
    List<CorrectOrder> findByUserIdAndBizStatusIn(long userId, List<Integer> bizStatus);

    /**
     * 根据旧订单ID查询新订单
     *
     * @param orderId
     * @param status
     * @return
     */
    List<CorrectOrder> findByOldOrderIdAndStatus(long orderId, int status);

    /**
     * 未批改完成订单
     *
     * @param orderIds
     * @param bizStatus
     * @return
     */
    long countByIdInAndBizStatusIn(List<Long> orderIds, List<Integer> bizStatus);

}
