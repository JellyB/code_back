package com.huatu.tiku.course.service.cache;

/**
 * Created by lijun on 2018/7/5
 */
public final class OrderCacheKey {

    /**
     * 立即购买 按钮
     */
    public static String orderPrevInfo() {
        return "_order:prevInfo:qps";
    }

    /**
     * 创建订单
     */
    public static String orderCreate() {
        return "_order:create:qps";
    }

    /**
     * 估分送课 人员名单
     */
    public static String zeroOrder(int courseId) {
        return "_order:zero:" + courseId;
    }
}
