package com.huatu.tiku.course.consts;

/**
 * @author hanchao
 * @date 2017/8/28 23:06
 */
public class NetSchoolUrlConst {
    /**
     * 全部直播列表接口
     */
    public static final String TOTAL_LIST = "/ztkClassSearch.php";

    /**
     * 我的直播
     */
    public static final String MY_LIST = "/myCourse_new.php";


    /**
     * 课程详情页接口
     */
    public static final String COURSE_DATAIL = "/Class_Details_Buy.php";

    /**
     * 课程详情页接口v2
     */
    public static final String COURSE_DATAIL_V2 = "/v2/Class_Details_Buy.php";

    /**
     * 课程详情页接口v2
     */
    public static final String COURSE_DATAIL_V2_SP = "/v2/Class_Details_Buy_new.php";

    /**
     * 添加免费课程
     */
    public static final String FREE_COURSE = "/freeOrder.php";

    /**
     * 物流列表
     */
    public static final String LOGISTICS = "/logisticsInfo.php";

    /**
     * 物流详情接口
     */
    public static final String LOGISTICS_QUERY = "/msg_comm/getInfo.php";


    /**
     * 我的直播列表隐藏课程(添加隐藏课程)
     */
    public static final String HIDE_COURSE = "/delClass.php";

    /**
     * 恢复隐藏课程
     */
    public static final String SHOW_COURSE = "/recoverClass.php";

    /**
     * 我的隐藏课程列表
     */
    public static final String HIDE_LIST = "/myHideClasses.php";


    /**
     * 我的直播课程详情页android
     */
    public static final String MY_COURSE_DATAIL_ANDROID = "/courseDetail_test.php";

    /**
     * 我的直播课程详情页ios
     */
    public static final String MY_COURSE_DATAIL_IOS = "/DetailIphone_tmp.php";


    /**
     * 支付详情接口
     */
    public static final String SALE_DETAIL = "/isSaleOut.php";

    /**
     * IOS创建订单
     */
    public static final String CREATE_ORDER_IOS = "/IOS/createOrderIOS.php";


    /**
     * android创建订单
     */
    public static final String CREATE_ORDER_ANDROID = "/createOrderAndroid.php";

    /**
     * 课程详情h5页面
     */
    public static final String COURSE_H5 = "/v3/h5/detail_zhuanti_contents.php";

    /**
     * 登陆送课，好评送课
     */
    public static final String LOGIN_COMMENT_COURSE = "/classForDownLoad.php";


    /**
     * 讲义列表
     */
    public static final String HANDOUT_LIST = "/handouts.php";

    /**
     * 课程合集接口
     */
    public static final String ALL_COLLECTION_LIST = "/v2/collectionClassSearch.php";

    /**
     * 拆分使用的课程合集接口
     */
    public static final String ALL_COLLECTION_LIST_SP = "/v2/collectionClassSearch_new.php";

    /**
     * 合集包含的课程接口
     */
    public static final String COLLECTION_DETAIL = "/v2/ztkClassSearch.php";

    /**
     * 我的直播-课程列表接口-suit
     */
    public static final String MY_LIVE_SUIT_LIST = "/v2/myCourse_new.php";

    /**
     * 我的直播-套餐课程主页接口
     */
    public static final String MY_LIVE_SUIT_DETAIL = "/v2/packageClassList.php";

    /**
     * ios内购回调接口
     */
    public static final String IOS_PAY_VERIFY = "/web_v/pay/notifyPcClassOrder.php?callback=iosPayVerify";
}
