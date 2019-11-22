package com.huatu.tiku.essay.essayEnum;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.huatu.tiku.essay.util.enu.EnumUtils;
import com.huatu.tiku.essay.util.enu.IEnum;

/**
 * Created by duanxiangchao on 2019/7/15
 */
public enum  OrderFeedBackEnum implements IEnum<Integer> {

    WAIT_FEED_BACK(0, "待评价"),
    FEED_BACK_FINISH(1, "已评价");

    private int value;
    private String title;

    public static OrderFeedBackEnum create(Integer value) {
        return (OrderFeedBackEnum)EnumUtils.getEnum(values(), value);
    }

    private OrderFeedBackEnum(int value, String title) {
        this.value = value;
        this.title = title;
    }
    public Integer getValue() {
        return this.value;
    }

    public String getTitle() {
        return this.title;
    }

}
