package com.huatu.tiku.essay.service.dispatch;

import com.huatu.tiku.essay.entity.correct.CorrectOrder;
import com.huatu.tiku.essay.entity.correct.CorrectOrderSnapshot;
import com.huatu.tiku.essay.essayEnum.CorrectOrderStatusEnum;

import java.util.List;

/**
 * @author huangqingpeng
 * @title: DispatchFilterService
 * @description: 派单筛选逻辑
 * @date 2019-07-2515:56
 */
public interface DispatchFilterService {

    /**
     * 是否还有派单次数
     * @param correctOrder
     * @return
     */
    boolean hasDispatchCount(CorrectOrder correctOrder);

    /**
     * 是否在派单时间内（顺延的当天不派单）
     * @param correctOrder
     * @return
     */
    boolean checkDispatchTime(CorrectOrder correctOrder);

    /**
     * 检查日志是否允许自动派单（是否未收到管理员干预）
     * @param correctOrder
     * @return
     */
    boolean checkDispatchSnapshot(CorrectOrder correctOrder);

    /**
     * 查询操作订单相关联的日志
     * @param id
     * @param dispatchAuto
     * @return
     */
    List<CorrectOrderSnapshot> findOperate(long id, CorrectOrderStatusEnum.OperateEnum dispatchAuto);
}
