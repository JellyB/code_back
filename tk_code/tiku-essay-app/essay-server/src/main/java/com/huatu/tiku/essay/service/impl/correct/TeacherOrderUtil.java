package com.huatu.tiku.essay.service.impl.correct;

import com.huatu.tiku.essay.entity.correct.CorrectOrder;
import com.huatu.tiku.essay.vo.admin.correct.CorrectOrderSimpleVO;
import lombok.extern.slf4j.Slf4j;

/**
 * @author huangqingpeng
 * @title: TeacherOrderUtil
 * @description: 教师人工批改订单相关处理
 * @date 2019-07-1515:28
 */
@Slf4j
public class TeacherOrderUtil {

    public static CorrectOrderSimpleVO convertOrderSimpleVO(CorrectOrder order) {
        CorrectOrderSimpleVO correctOrderSimpleVO = new CorrectOrderSimpleVO();
        if (null != order) {
            correctOrderSimpleVO = CorrectOrderSimpleVO.builder()
                    .expireTime(order.getGmtDeadLine() == null ? 0L : order.getGmtDeadLine().getTime())
                    .orderId(order.getId())
                    .receiveOrderTeacher(order.getReceiveOrderTeacher())
                    .build();
        }
        log.info("订单信息是:{}", correctOrderSimpleVO);
        return correctOrderSimpleVO;
    }

}
