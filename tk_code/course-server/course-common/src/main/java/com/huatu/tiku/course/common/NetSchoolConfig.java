package com.huatu.tiku.course.common;

/**
 * Created by linkang on 11/30/16.
 */
public class NetSchoolConfig {


    /**
     * 改为大于等于0，都视为成功
     * 网校接口成功请求code
     */
    public static final int SUCCESS_CODE = 0;

    /**
     * 请求php严禁的success code
     */
    public static final int HARD_SUCCESS_CODE = 10000;

    /**
     * IOS审核值
     */
    public static final int IOS_AUDIT_VALUE = 11;

    /**
     * 网校考试类型，公务员
     */
    public static final int CATEGORY_GWY = 1000;

    /**
     *网校考试类型,事业单位
     */
    public static final int CATEGORY_SHIYE = 8;

    /**
     * 微信支付type
     */
    public static final int WEIXIN_PAY = 33;

    /**
     * 支付宝
     */
    public static final int ALI_PAY = 32;

    /**
     * 苹果内购type
     */
    public static final int APPLE_IN_PURCHASE_PAY = 35;


    /**
     * 网校回调接口
     * 安全校验码
     */
    public static final String IOS_PAY_VERIFY_SECURITY_CODE = "h2xtuapm1yeqmsueiwtrxiz70okmnhy6";

    public static final String API_KEY = "0987654321!#%&(@$^*";
}
