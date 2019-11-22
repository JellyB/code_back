package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.EssayTeacherOrderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Created by duanxiangchao on 2019/7/10
 */
public interface EssayTeacherOrderTypeRepository extends JpaRepository<EssayTeacherOrderType, Long>, JpaSpecificationExecutor<EssayTeacherOrderType> {

    List<EssayTeacherOrderType> findByStatusAndTeacherIdIn(int status, List<Long> teacherIds);

    List<EssayTeacherOrderType> findByStatusAndTeacherId(int status,Long teacherId);

    EssayTeacherOrderType findByTeacherIdAndOrderTypeAndStatus(long teacherId, int orderType, int status);
    
    EssayTeacherOrderType findByTeacherIdAndOrderType(long teacherId, int orderType);

    @Transactional
    @Modifying
    @Query("update EssayTeacherOrderType tot set tot.status= -1  where tot.teacherId = ?1 ")
    int deleteById(Long teacherId);

    /**
     * 查看老师工作量是否饱和
     * @param orderType
     * @param receiptStatus
     * @param receiptRate
     * @param orderLimit
     * @return
     */
	long countByOrderTypeAndReceiptStatusAndReceiptRateLessThanAndOrderLimitGreaterThan(int orderType, int receiptStatus, int receiptRate,int orderLimit);

	List<EssayTeacherOrderType> findByOrderTypeAndReceiptStatusAndStatusAndReceiptRateLessThanAndOrderLimitGreaterThan(int orderType, int receiptStatus, int status, int receiptRate, int orderLimit);

	@Transactional
    @Modifying
    @Query("update EssayTeacherOrderType tot set tot.receiptStatus= ?1  where tot.teacherId = ?2 and tot.orderType= ?3 ")
	int updateReceiptStatusByTeacherIdAndOrderType(int receiptStatus, long teacherId, int orderType);

    /**
     * 查询清空任务量时间小于当天开始时间的统计数据
     * @param status
     * @param todayStart
     * @return
     */
    List<EssayTeacherOrderType> findByStatusAndGmtClearLessThanEqual(int status, Date todayStart);
}
