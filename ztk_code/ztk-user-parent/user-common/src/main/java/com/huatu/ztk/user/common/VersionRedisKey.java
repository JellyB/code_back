package com.huatu.ztk.user.common;

/**
 * 保存版本信息的redis key
 * Created by linkang on 8/5/16.
 */
public class VersionRedisKey {
    public static final String ANDROID_LATEST_VERSION_KEY = "android_latest_version";

    public static final String ANDROID_FULL_URL_KEY = "android_full_url";

    public static final String ANDROID_BULK_URL_KEY = "android_bulk_url";

    public static final String IOS_LATEST_VERSION_KEY = "ios_lastest_version";

    public static final String VERSION_MESSAGE_KEY = "version_message";

    public static final String MOD_VALUE_KEY = "mod_value";

    public static final String IOS_AUDIT_AD_OPEN_KEY = "i_a_o_a";

    public static final String IOS_AUDIT_AD_KEY = "ios_audit_ad";
     /* 最新的版本对象信息  */
    public static final String LATEST_VERSION_OBJ_PREFIX = "app_version_latest_";
    public static final String CURRENT_VERSION_OBJ_PREFIX = "app_version_curr_";

    private static final String IOS_AUDIT = "ios_audit_versions";
    /**
     * ios审核版本redis key
     * catgory 参数忽略（每一个科目的审核版本号一致，不用多次设置）
     * @param catgory 科目
     * @return
     */
    public static String getIosAuditSetKey(int catgory) {
//        return "ios_audit_versions_" + catgory;
        return IOS_AUDIT;
    }

    /**
     * ios审核版本redis key
     * @return
     */
    public static String getIosAuditSetKey() {
        return IOS_AUDIT;
    }
    /**
     * ios 是否开启轮播广告图
     * @return
     */
    public static String getIosAdKey() {
        return IOS_AUDIT_AD_KEY;
    }
}
