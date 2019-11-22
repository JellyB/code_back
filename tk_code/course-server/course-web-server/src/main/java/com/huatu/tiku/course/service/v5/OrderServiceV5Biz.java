package com.huatu.tiku.course.service.v5;

import java.util.HashMap;

/**
 * Created by lijun on 2018/10/22
 */
public interface OrderServiceV5Biz {

    /**
     * 用户 估分赠课
     * @return 操作结果
     */
    Object bigGiftOrder(HashMap<String, Object> map);

    /**
     * 用户是否已经 领取了某个估分大礼包
     *
     * @param classId  课程ID
     * @param userName 用户名
     * @return 领取 返回trueo
     */
    boolean hasGetBigGiftOrder(Integer classId, String userName);
}
