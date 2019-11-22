package com.huatu.tiku.essay.repository.snapshot;

import com.huatu.tiku.essay.entity.correct.CorrectOrderSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * @author huangqingpeng
 * @title: CorrectOrderSnapshotRepository
 * @description: 订单行为日志底层实现
 * @date 2019-07-1715:06
 */
public interface CorrectOrderSnapshotRepository extends JpaRepository<CorrectOrderSnapshot, Long>,JpaSpecificationExecutor<CorrectOrderSnapshot> {

    List<CorrectOrderSnapshot>  findByOrderIdAndStatus(long orderId,int status);

    long countByOrderIdAndChannel(long orderId,int channel);

    List<CorrectOrderSnapshot> findByOrderIdAndOperateAndStatus(long orderId,int operate,int status);
}
