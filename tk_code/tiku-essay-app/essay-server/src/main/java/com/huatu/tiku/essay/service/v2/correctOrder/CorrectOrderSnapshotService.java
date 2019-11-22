package com.huatu.tiku.essay.service.v2.correctOrder;

import com.huatu.tiku.essay.entity.correct.CorrectOrder;
import com.huatu.tiku.essay.entity.correct.CorrectOrderSnapshot;
import com.huatu.tiku.essay.essayEnum.CorrectOrderStatusEnum;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.vo.admin.correct.CorrectOrderSnapshotVo;

import java.util.List;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/7/17
 * @描述 订单流转记录
 */
public interface CorrectOrderSnapshotService {

    /**
     * 查询订单行为记录
     *
     * @param orderId
     * @return
     */
    PageUtil<CorrectOrderSnapshotVo> getOrderSnapshot(long orderId, int page, int pageSize);

    void save(CorrectOrderSnapshot correctOrderSnapshot, CorrectOrderStatusEnum.OperateEnum operateEnum);

    /**
     * 检查订单是否由管理员经手过 true表示没有管理员经手，否则经手过
     * @param correctOrder
     * @return
     */
    boolean checkNoAdmin(CorrectOrder correctOrder);

    /**
     * 查询订单某种操作日志
     * @param orderId
     * @param operate
     * @return
     */
    List<CorrectOrderSnapshot> findByOrderIdAndOperate(long orderId, CorrectOrderStatusEnum.OperateEnum operate);


}


