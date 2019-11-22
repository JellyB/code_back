package com.huatu.tiku.course.common;

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


    /**
     * ios审核版本redis key
     * @param catgory 科目
     * @return
     */
    public static String getIosAuditSetKey(int catgory) {
        return "ios_audit_versions_" + catgory;
    }

    public static String getIosAuditSetKey(){
        return "ios_audit_versions";
    }
}
